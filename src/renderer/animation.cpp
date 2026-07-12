#include "lu/renderer/animation.h"

#include <algorithm>
#include <cmath>
#include <cctype>
#include <functional>
#include <unordered_map>

namespace lu::renderer {

namespace {

std::string lowerCopy(std::string value) {
    std::transform(value.begin(), value.end(), value.begin(),
        [](unsigned char c) { return static_cast<char>(std::tolower(c)); });
    return value;
}

template <typename Key>
size_t keySegment(const std::vector<Key>& keys, float time) {
    if (keys.size() < 2 || time <= keys.front().time) return 0;
    auto upper = std::upper_bound(keys.begin(), keys.end(), time,
        [](float value, const Key& key) { return value < key.time; });
    if (upper == keys.end()) return keys.size() - 2;
    return static_cast<size_t>(std::distance(keys.begin(), upper) - 1);
}

float segmentAmount(float time, float first, float second) {
    const float duration = second - first;
    if (duration <= 0.000001f) return 0.0f;
    return std::clamp((time - first) / duration, 0.0f, 1.0f);
}

float sampleFloat(const std::vector<AnimationFloatKey>& keys, float time, float fallback) {
    if (keys.empty()) return fallback;
    if (keys.size() == 1 || time <= keys.front().time) return keys.front().value;
    if (time >= keys.back().time) return keys.back().value;
    const size_t index = keySegment(keys, time);
    const auto& a = keys[index];
    const auto& b = keys[index + 1];
    const float t = segmentAmount(time, a.time, b.time);
    return a.value + (b.value - a.value) * t;
}

Vec3 sampleVec3(const AnimationTransformTrack& track, float time, Vec3 fallback) {
    const auto& keys = track.translation_keys;
    if (keys.empty()) return fallback;
    if (keys.size() == 1 || time <= keys.front().time) return keys.front().value;
    if (time >= keys.back().time) return keys.back().value;
    const size_t index = keySegment(keys, time);
    const auto& a = keys[index];
    const auto& b = keys[index + 1];
    const float t = segmentAmount(time, a.time, b.time);
    if (track.translation_key_type == 2) {
        const float t2 = t * t;
        const float t3 = t2 * t;
        const float duration = b.time - a.time;
        return a.value * (2.0f * t3 - 3.0f * t2 + 1.0f) +
               a.forward_tangent * (duration * (t3 - 2.0f * t2 + t)) +
               b.value * (-2.0f * t3 + 3.0f * t2) +
               b.backward_tangent * (duration * (t3 - t2));
    }
    return lerp(a.value, b.value, t);
}

Quat axisAngle(float angle, Vec3 axis) {
    const float half = angle * 0.5f;
    const float sine = std::sin(half);
    return {axis.x * sine, axis.y * sine, axis.z * sine, std::cos(half)};
}

Quat sampleRotation(const AnimationTransformTrack& track, float time, Quat fallback) {
    if (!track.rotation_keys.empty()) {
        const auto& keys = track.rotation_keys;
        if (keys.size() == 1 || time <= keys.front().time) return keys.front().value;
        if (time >= keys.back().time) return keys.back().value;
        const size_t index = keySegment(keys, time);
        const auto& a = keys[index];
        const auto& b = keys[index + 1];
        return slerp(a.value, b.value, segmentAmount(time, a.time, b.time));
    }
    if (!track.euler_x_keys.empty() || !track.euler_y_keys.empty() || !track.euler_z_keys.empty()) {
        const float x = sampleFloat(track.euler_x_keys, time, 0.0f);
        const float y = sampleFloat(track.euler_y_keys, time, 0.0f);
        const float z = sampleFloat(track.euler_z_keys, time, 0.0f);
        return axisAngle(x, {1.0f, 0.0f, 0.0f}) *
               axisAngle(y, {0.0f, 1.0f, 0.0f}) *
               axisAngle(z, {0.0f, 0.0f, 1.0f});
    }
    return fallback;
}

bool validScalar(float value) {
    return std::isfinite(value) && std::abs(value) < 1.0e20f;
}

NodeTransform evaluateTrack(
    const AnimationTransformTrack& track,
    const NodeTransform& bind,
    float time) {

    NodeTransform fallback = bind;
    if (validScalar(track.default_transform.translation.x) &&
        validScalar(track.default_transform.translation.y) &&
        validScalar(track.default_transform.translation.z)) {
        fallback.translation = track.default_transform.translation;
    }
    const Quat default_rotation = track.default_transform.rotation;
    const float rotation_length_sq =
        default_rotation.x * default_rotation.x +
        default_rotation.y * default_rotation.y +
        default_rotation.z * default_rotation.z +
        default_rotation.w * default_rotation.w;
    if (validScalar(rotation_length_sq) && rotation_length_sq > 0.000001f) {
        fallback.rotation = normalize(default_rotation);
    }
    if (validScalar(track.default_transform.scale) &&
        std::abs(track.default_transform.scale) > 0.000001f) {
        fallback.scale = track.default_transform.scale;
    }

    NodeTransform result;
    result.translation = sampleVec3(track, time, fallback.translation);
    result.rotation = sampleRotation(track, time, fallback.rotation);
    result.scale = sampleFloat(track.scale_keys, time, fallback.scale);
    return result;
}

} // namespace

float animationSampleTime(const AnimationClip& clip, float playback_seconds) {
    const float duration = std::max(0.0f, clip.stop_time - clip.start_time);
    if (duration <= 0.000001f) return clip.start_time;
    const float elapsed = std::max(0.0f, playback_seconds * clip.frequency);
    if (clip.cycle_type == 2) {
        return clip.start_time + std::min(elapsed, duration);
    }
    if (clip.cycle_type == 1 || clip.cycle_type == 4) {
        const float phase = std::fmod(elapsed, duration * 2.0f);
        return clip.start_time + (phase <= duration ? phase : duration * 2.0f - phase);
    }
    return clip.start_time + std::fmod(elapsed, duration);
}

AnimationPose evaluateAnimationPose(
    const RenderWorld& world,
    size_t clip_index,
    float playback_seconds) {

    AnimationPose pose;
    pose.clip_index = clip_index;
    pose.node_world_matrices.resize(world.skeleton_nodes.size(), identity());
    if (world.skeleton_nodes.empty()) return pose;

    std::vector<NodeTransform> local_transforms;
    local_transforms.reserve(world.skeleton_nodes.size());
    std::unordered_map<uint32_t, size_t> node_by_block;
    std::unordered_map<std::string, size_t> node_by_name;
    for (size_t i = 0; i < world.skeleton_nodes.size(); ++i) {
        const SkeletonNode& node = world.skeleton_nodes[i];
        local_transforms.push_back(node.bind_local);
        node_by_block[node.block_index] = i;
        node_by_name.emplace(lowerCopy(node.name), i);
    }

    if (clip_index < world.animation.clips.size()) {
        const AnimationClip& clip = world.animation.clips[clip_index];
        pose.sample_time = animationSampleTime(clip, playback_seconds);
        for (const AnimationControlledBlock& block : clip.controlled_blocks) {
            if (!block.transform_track.present) continue;
            auto target = node_by_name.find(lowerCopy(block.node_name));
            if (target == node_by_name.end()) continue;
            local_transforms[target->second] = evaluateTrack(
                block.transform_track,
                world.skeleton_nodes[target->second].bind_local,
                pose.sample_time);
        }
    }

    std::vector<uint8_t> state(world.skeleton_nodes.size(), 0);
    std::function<Mat4(size_t)> evaluate_world = [&](size_t index) -> Mat4 {
        if (state[index] == 2) return pose.node_world_matrices[index];
        const Mat4 local = transform_matrix(
            local_transforms[index].translation,
            local_transforms[index].rotation,
            local_transforms[index].scale);
        if (state[index] == 1) return local;
        state[index] = 1;
        const int32_t parent_block = world.skeleton_nodes[index].parent_block;
        auto parent = parent_block >= 0
            ? node_by_block.find(static_cast<uint32_t>(parent_block))
            : node_by_block.end();
        pose.node_world_matrices[index] = parent == node_by_block.end()
            ? local
            : evaluate_world(parent->second) * local;
        state[index] = 2;
        return pose.node_world_matrices[index];
    };
    for (size_t i = 0; i < world.skeleton_nodes.size(); ++i) evaluate_world(i);
    return pose;
}

} // namespace lu::renderer

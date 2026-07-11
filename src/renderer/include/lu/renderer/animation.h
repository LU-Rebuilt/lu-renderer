#pragma once

#include "lu/renderer/render_types.h"

namespace lu::renderer {

float animationSampleTime(const AnimationClip& clip, float playback_seconds);
AnimationPose evaluateAnimationPose(
    const RenderWorld& world,
    size_t clip_index,
    float playback_seconds);

} // namespace lu::renderer

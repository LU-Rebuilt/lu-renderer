#pragma once

#include <algorithm>
#include <array>
#include <cmath>

namespace lu::renderer {

struct Vec2 {
    float x = 0.0f;
    float y = 0.0f;
};

struct Vec3 {
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;
};

struct Vec4 {
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;
    float w = 0.0f;
};

struct Quat {
    float x = 0.0f;
    float y = 0.0f;
    float z = 0.0f;
    float w = 1.0f;
};

struct Mat4 {
    std::array<float, 16> m{};
};

inline Vec3 operator+(Vec3 a, Vec3 b) { return {a.x + b.x, a.y + b.y, a.z + b.z}; }
inline Vec3 operator-(Vec3 a, Vec3 b) { return {a.x - b.x, a.y - b.y, a.z - b.z}; }
inline Vec3 operator*(Vec3 v, float s) { return {v.x * s, v.y * s, v.z * s}; }
inline Vec3 operator/(Vec3 v, float s) { return {v.x / s, v.y / s, v.z / s}; }
inline Vec3 lerp(Vec3 a, Vec3 b, float t) { return a + (b - a) * t; }

inline float dot(Vec3 a, Vec3 b) {
    return a.x * b.x + a.y * b.y + a.z * b.z;
}

inline Vec3 cross(Vec3 a, Vec3 b) {
    return {
        a.y * b.z - a.z * b.y,
        a.z * b.x - a.x * b.z,
        a.x * b.y - a.y * b.x
    };
}

inline float length(Vec3 v) {
    return std::sqrt(dot(v, v));
}

inline Vec3 normalize(Vec3 v) {
    float len = length(v);
    if (len <= 0.000001f) return {0.0f, 1.0f, 0.0f};
    return v / len;
}

inline Mat4 identity() {
    Mat4 out{};
    out.m[0] = 1.0f;
    out.m[5] = 1.0f;
    out.m[10] = 1.0f;
    out.m[15] = 1.0f;
    return out;
}

inline Quat normalize(Quat q) {
    const float len_sq = q.x * q.x + q.y * q.y + q.z * q.z + q.w * q.w;
    if (len_sq <= 0.000001f) return {};
    const float inv_len = 1.0f / std::sqrt(len_sq);
    return {q.x * inv_len, q.y * inv_len, q.z * inv_len, q.w * inv_len};
}

inline Quat operator*(Quat a, Quat b) {
    return {
        a.w * b.x + a.x * b.w + a.y * b.z - a.z * b.y,
        a.w * b.y - a.x * b.z + a.y * b.w + a.z * b.x,
        a.w * b.z + a.x * b.y - a.y * b.x + a.z * b.w,
        a.w * b.w - a.x * b.x - a.y * b.y - a.z * b.z
    };
}

inline Quat slerp(Quat a, Quat b, float t) {
    a = normalize(a);
    b = normalize(b);
    float cos_theta = a.x * b.x + a.y * b.y + a.z * b.z + a.w * b.w;
    if (cos_theta < 0.0f) {
        b = {-b.x, -b.y, -b.z, -b.w};
        cos_theta = -cos_theta;
    }
    if (cos_theta > 0.9995f) {
        return normalize(Quat{
            a.x + (b.x - a.x) * t,
            a.y + (b.y - a.y) * t,
            a.z + (b.z - a.z) * t,
            a.w + (b.w - a.w) * t
        });
    }
    const float theta = std::acos(std::clamp(cos_theta, -1.0f, 1.0f));
    const float sin_theta = std::sin(theta);
    const float wa = std::sin((1.0f - t) * theta) / sin_theta;
    const float wb = std::sin(t * theta) / sin_theta;
    return {
        a.x * wa + b.x * wb,
        a.y * wa + b.y * wb,
        a.z * wa + b.z * wb,
        a.w * wa + b.w * wb
    };
}

inline Mat4 operator*(const Mat4& a, const Mat4& b) {
    Mat4 out{};
    for (int column = 0; column < 4; ++column) {
        for (int row = 0; row < 4; ++row) {
            for (int k = 0; k < 4; ++k) {
                out.m[column * 4 + row] +=
                    a.m[k * 4 + row] * b.m[column * 4 + k];
            }
        }
    }
    return out;
}

inline Mat4 transform_matrix(Vec3 translation, Quat rotation, float scale) {
    rotation = normalize(rotation);
    const float xx = rotation.x * rotation.x;
    const float yy = rotation.y * rotation.y;
    const float zz = rotation.z * rotation.z;
    const float xy = rotation.x * rotation.y;
    const float xz = rotation.x * rotation.z;
    const float yz = rotation.y * rotation.z;
    const float xw = rotation.x * rotation.w;
    const float yw = rotation.y * rotation.w;
    const float zw = rotation.z * rotation.w;

    Mat4 out = identity();
    out.m[0] = (1.0f - 2.0f * (yy + zz)) * scale;
    out.m[1] = (2.0f * (xy + zw)) * scale;
    out.m[2] = (2.0f * (xz - yw)) * scale;
    out.m[4] = (2.0f * (xy - zw)) * scale;
    out.m[5] = (1.0f - 2.0f * (xx + zz)) * scale;
    out.m[6] = (2.0f * (yz + xw)) * scale;
    out.m[8] = (2.0f * (xz + yw)) * scale;
    out.m[9] = (2.0f * (yz - xw)) * scale;
    out.m[10] = (1.0f - 2.0f * (xx + yy)) * scale;
    out.m[12] = translation.x;
    out.m[13] = translation.y;
    out.m[14] = translation.z;
    return out;
}

inline Mat4 inverse_transform_matrix(Vec3 translation, Quat rotation, float scale) {
    const float inverse_scale = std::abs(scale) > 0.000001f ? 1.0f / scale : 1.0f;
    rotation = normalize(rotation);
    const Quat inverse_rotation{-rotation.x, -rotation.y, -rotation.z, rotation.w};
    const Mat4 inverse_rs = transform_matrix({}, inverse_rotation, inverse_scale);
    Mat4 inverse_translation = identity();
    inverse_translation.m[12] = -translation.x;
    inverse_translation.m[13] = -translation.y;
    inverse_translation.m[14] = -translation.z;
    return inverse_rs * inverse_translation;
}

inline Vec3 transform_point(const Mat4& matrix, Vec3 point) {
    return {
        matrix.m[0] * point.x + matrix.m[4] * point.y + matrix.m[8] * point.z + matrix.m[12],
        matrix.m[1] * point.x + matrix.m[5] * point.y + matrix.m[9] * point.z + matrix.m[13],
        matrix.m[2] * point.x + matrix.m[6] * point.y + matrix.m[10] * point.z + matrix.m[14]
    };
}

inline Mat4 look_at(Vec3 eye, Vec3 target, Vec3 up) {
    Vec3 z = normalize(target - eye);
    Vec3 x = normalize(cross(up, z));
    Vec3 y = cross(z, x);

    Mat4 out = identity();
    out.m[0] = x.x; out.m[4] = x.y; out.m[8]  = x.z; out.m[12] = -dot(x, eye);
    out.m[1] = y.x; out.m[5] = y.y; out.m[9]  = y.z; out.m[13] = -dot(y, eye);
    out.m[2] = z.x; out.m[6] = z.y; out.m[10] = z.z; out.m[14] = -dot(z, eye);
    return out;
}

inline Mat4 perspective(float fov_y_radians, float aspect, float near_plane, float far_plane) {
    float f = 1.0f / std::tan(fov_y_radians * 0.5f);
    Mat4 out{};
    out.m[0] = f / aspect;
    out.m[5] = f;
    out.m[10] = far_plane / (far_plane - near_plane);
    out.m[11] = 1.0f;
    out.m[14] = -(near_plane * far_plane) / (far_plane - near_plane);
    return out;
}

} // namespace lu::renderer

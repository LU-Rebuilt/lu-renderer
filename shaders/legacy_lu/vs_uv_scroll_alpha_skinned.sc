$input a_position, a_normal, a_indices, a_weight, a_texcoord0, a_color0
$output v_normal, v_texcoord0, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform mat4 u_skinBones[64];
uniform vec4 u_luEffectTime;
uniform vec4 u_luUvMotion1;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    float weightSum = dot(a_weight, vec4_splat(1.0));
    vec4 skinnedPos = localPos;
    if (weightSum > 0.00001) {
        skinnedPos =
            mul(u_skinBones[int(a_indices.x + 0.5)], localPos) * a_weight.x +
            mul(u_skinBones[int(a_indices.y + 0.5)], localPos) * a_weight.y +
            mul(u_skinBones[int(a_indices.z + 0.5)], localPos) * a_weight.z +
            mul(u_skinBones[int(a_indices.w + 0.5)], localPos) * a_weight.w;
    }
    gl_Position = mul(u_viewProj, skinnedPos);
    v_normal = a_normal;
    v_texcoord0 = a_texcoord0 + (u_luUvMotion1.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_color0 = a_color0;
    v_worldPos = skinnedPos;
}

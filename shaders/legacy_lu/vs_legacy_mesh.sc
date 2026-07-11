$input a_position, a_normal, a_indices, a_weight, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform mat4 u_skinBones[64];

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec4 worldPos = mul(u_model[0], localPos);
    vec3 worldNormal = normalize(mul(u_model[0], vec4(a_normal, 0.0)).xyz);
    if (dot(a_weight, vec4_splat(1.0)) > 0.00001) {
        worldPos =
            mul(u_skinBones[int(a_indices.x + 0.5)], localPos) * a_weight.x +
            mul(u_skinBones[int(a_indices.y + 0.5)], localPos) * a_weight.y +
            mul(u_skinBones[int(a_indices.z + 0.5)], localPos) * a_weight.z +
            mul(u_skinBones[int(a_indices.w + 0.5)], localPos) * a_weight.w;
        worldNormal = normalize(
            mul(u_skinBones[int(a_indices.x + 0.5)], vec4(a_normal, 0.0)).xyz * a_weight.x +
            mul(u_skinBones[int(a_indices.y + 0.5)], vec4(a_normal, 0.0)).xyz * a_weight.y +
            mul(u_skinBones[int(a_indices.z + 0.5)], vec4(a_normal, 0.0)).xyz * a_weight.z +
            mul(u_skinBones[int(a_indices.w + 0.5)], vec4(a_normal, 0.0)).xyz * a_weight.w);
    }
    gl_Position = mul(u_viewProj, worldPos);
    v_normal = worldNormal;
    v_texcoord0 = a_texcoord0;
    v_texcoord1 = a_texcoord1;
    v_color0 = a_color0;
    v_worldPos = worldPos;
}

$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_materialDiffuse;
uniform vec4 u_materialEmissive;
uniform vec4 u_luShinyGlint;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    float fadeValue = clamp(a_position.y - u_luShinyGlint.x, 0.0, 1.0) * 100.0;
    fadeValue = clamp(pow(fadeValue, 100.0), 0.0, 1.0);
    vec4 modelTint = vec4(a_color0.rgb, mix(1.0, 0.5, fadeValue));
    vec4 materialColor = vec4(u_materialDiffuse.rgb, 1.0 / max(u_materialDiffuse.a, 0.00001));

    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = a_normal;
    v_texcoord0 = vec2_splat(0.0);
    v_texcoord1 = vec2_splat(0.0);
    v_color0 = mix(modelTint, materialColor, u_materialEmissive.r);
    v_worldPos = mul(u_model[0], localPos);
}

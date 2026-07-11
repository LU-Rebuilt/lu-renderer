$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>
#include "alpha_test.sh"

SAMPLER2D(s_diffuse, 0);

uniform vec4 u_materialDiffuse;
uniform vec4 u_luShaderFlags;
uniform vec4 u_luEffectTime;

void main()
{
    vec4 texColor = texture2D(s_diffuse, v_texcoord0);
    vec4 textured = texColor * v_color0;
    vec4 untextured = v_color0;
    vec4 color = (textured * u_luShaderFlags.x) + (untextured * (1.0 - u_luShaderFlags.x));

    // Basic_AnimAlpha_PS and Basic_NoTexture_AnimAlpha_PS take animated alpha
    // from the material rather than the texture. attr_fadeEffectAlphaValue is
    // represented by the ordinary per-material fade value (currently 1).
    color.a = mix(color.a, v_color0.a * u_materialDiffuse.a, u_luEffectTime.z);

    if (!luAlphaTestPass(color.a)) {
        discard;
    }

    gl_FragColor = color;
}

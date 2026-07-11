$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>
#include "alpha_test.sh"

SAMPLER2D(s_diffuse, 0);

// x/y/z correspond to LU's g_movementOffset, g_alphaMovement, and
// g_fuzzOffset. The recovered effect supplies them as engine globals.
uniform vec4 u_luUvMotion1;

void main()
{
    vec2 movedUv = v_texcoord0 + vec2_splat(u_luUvMotion1.x);
    vec2 alphaUv = v_texcoord0 + vec2_splat(u_luUvMotion1.y);
    vec2 fuzzUv;
    fuzzUv.x = fract(v_texcoord0.x + u_luUvMotion1.z) * 0.8 + 0.1;
    fuzzUv.y = fract(v_texcoord0.y + u_luUvMotion1.z) * 0.6 + 0.2;

    vec4 result = texture2D(s_diffuse, movedUv);
    vec4 fuzz = texture2D(s_diffuse, fuzzUv);
    vec4 alpha = texture2D(s_diffuse, alphaUv);
    result.rgb *= fuzz.a;
    result.rgb += vec3(0.2, 0.2, 0.0) * v_texcoord1.x;
    result.a = alpha.a;

    if (!luAlphaTestPass(result.a)) {
        discard;
    }
    gl_FragColor = result;
}

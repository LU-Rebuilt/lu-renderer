$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>
#include "alpha_test.sh"

SAMPLER2D(s_diffuse, 0);
SAMPLER2D(s_dark, 2);

void main()
{
    vec4 color = texture2D(s_diffuse, v_texcoord0) * v_color0;
    color.a = texture2D(s_dark, v_texcoord1).a;
    if (!luAlphaTestPass(color.a)) {
        discard;
    }
    gl_FragColor = color;
}

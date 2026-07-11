$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>
#include "alpha_test.sh"

void main()
{
    vec4 color = v_color0 * 1.65;
    if (!luAlphaTestPass(color.a)) {
        discard;
    }
    gl_FragColor = color;
}

$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>
#include "alpha_test.sh"

void main()
{
    if (!luAlphaTestPass(v_color0.a)) {
        discard;
    }
    gl_FragColor = v_color0;
}

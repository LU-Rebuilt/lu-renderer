$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

void main()
{
    gl_FragColor = v_color0;
}

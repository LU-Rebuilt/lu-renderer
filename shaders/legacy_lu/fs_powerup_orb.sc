$input v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos, v_reflectVector, v_vdn, v_diffuse, v_specular

#include <bgfx_shader.sh>
#include "alpha_test.sh"

SAMPLERCUBE(s_luEnv, 1);

void main()
{
    vec4 reflection = textureCube(s_luEnv, vec3(v_reflectVector.x, -v_reflectVector.y, v_reflectVector.z));
    float fresnel = (0.1 + 0.9 * pow(max(1.0 - abs(v_vdn), 0.0), 3.5)) * 1.5;
    reflection.rgb += vec3_splat(fresnel);
    reflection *= 2.45;

    vec4 diffuse = v_diffuse;
    diffuse.rgb *= reflection.rgb;
    vec4 color = diffuse + v_specular + reflection * 0.05;
    if (!luAlphaTestPass(color.a)) {
        discard;
    }
    gl_FragColor = color;
}

$input v_texcoord0, v_color0, v_reflectVector, v_diffuseExtra

#include <bgfx_shader.sh>

SAMPLER2D(s_normal, 4);

uniform vec4 u_luLightColorShadow;
uniform vec4 u_luAmbient;
uniform vec4 u_luUpperHemi;
uniform vec4 u_luLowerHemi;
uniform vec4 u_luSpecular;

vec3 calculateHemiLightInfluence(vec3 normal)
{
    float shiftedY = normal.y + 1.0;
    return ((u_luUpperHemi.rgb * shiftedY) + (u_luLowerHemi.rgb * (2.0 - shiftedY))) * 0.5;
}

float calculateFresnel(float vdn)
{
    return (0.1 + 0.9 * pow(max(0.0, 1.0 - min(1.0, abs(vdn))), 3.5)) * 0.8;
}

void main()
{
    float depth = texture2D(s_normal, v_texcoord0).a;
    vec2 offsetVector = -v_reflectVector.xy * depth * 0.04;
    depth = (depth + texture2D(s_normal, v_texcoord0 + offsetVector).a) * 0.5;
    offsetVector = -v_reflectVector.xy * depth * 0.05;
    depth = (depth + texture2D(s_normal, v_texcoord0 + offsetVector).a) * 0.5;
    offsetVector = -v_reflectVector.xy * depth * 0.06;

    vec3 normal = normalize((texture2D(s_normal, v_texcoord0 + offsetVector).rgb - vec3_splat(0.5)) * 2.0);
    float diffuseContribution = dot(normal, v_diffuseExtra);
    vec3 hemiLight = calculateHemiLightInfluence(normal);
    float reflectionContribution = dot(v_reflectVector, normal);
    float fresnel = calculateFresnel(reflectionContribution);
    vec3 halfVector = normalize(v_reflectVector + v_diffuseExtra);
    float hdn = pow(max(0.0, dot(halfVector, normal)), 320.0);
    vec3 specular = diffuseContribution * hdn * u_luSpecular.rgb;

    vec3 color = max(0.0, diffuseContribution) * hemiLight * u_luLightColorShadow.rgb + u_luAmbient.rgb + vec3_splat(fresnel);
    color *= v_color0.rgb;
    color += specular;
    gl_FragColor = vec4(color, 1.0);
}

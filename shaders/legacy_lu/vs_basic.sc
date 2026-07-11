$input a_position, a_normal, a_texcoord0, a_texcoord1, a_color0
$output v_normal, v_texcoord0, v_texcoord1, v_color0, v_worldPos

#include <bgfx_shader.sh>

uniform vec4 u_materialDiffuse;
uniform vec4 u_lightDirAmbient;
uniform vec4 u_lightColor;
uniform vec4 u_luShaderFlags;
uniform vec4 u_luEffectTime;
uniform vec4 u_luUvMotion1;

void main()
{
    vec4 localPos = vec4(a_position, 1.0);
    vec4 worldPos = mul(u_model[0], localPos);
    vec3 worldNormal = normalize(mul(u_model[0], vec4(a_normal, 0.0)).xyz);

    gl_Position = mul(u_modelViewProj, localPos);
    v_normal = worldNormal;
    v_texcoord0 = a_texcoord0 + (u_luUvMotion1.xy * u_luEffectTime.x * u_luEffectTime.y);
    v_texcoord1 = a_texcoord1;
    v_worldPos = worldPos;

    vec4 sourceColor = vec4(1.0, 1.0, 1.0, 1.0);
    sourceColor = mix(sourceColor, a_color0, u_luShaderFlags.y);
    sourceColor = mix(sourceColor, u_materialDiffuse, u_luShaderFlags.z);

    vec3 lightDir = normalize(-u_lightDirAmbient.xyz);
    float lightContribution = max(dot(lightDir, worldNormal), 0.0);
    vec3 litColor = (u_lightColor.rgb * lightContribution) + vec3_splat(u_lightDirAmbient.w);
    sourceColor.rgb = mix(sourceColor.rgb, sourceColor.rgb * litColor, u_luEffectTime.w);

    // BasicShaders.fx computes lighting and vertex-color modulation in the
    // vertex shader, then passes that result directly to the pixel shader.
    v_color0 = sourceColor;
}

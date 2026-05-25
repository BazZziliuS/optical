
#version 150

in vec3 Position;
in vec4 Color;
in vec2 UV0;
in ivec2 UV1;      // lightmap
in ivec2 UV2;      // overlay
in vec3 Normal;

uniform mat4 ModelViewMat;
uniform mat4 ProjMat;
uniform float Time;

out vec4 vertexColor;
out vec2 texCoord0;
out vec4 lightMapColor;

void main() {
    gl_Position = ProjMat * ModelViewMat * vec4(Position, 1.0);

    // mesma estrutura do glint vanilla, mas com sensação de movimento
    texCoord0 = UV0 + vec2(0.0, Time * 0.2);
    vertexColor = Color;
    lightMapColor = vec4(UV1.x / 256.0, UV1.y / 256.0, 1.0, 1.0);
}

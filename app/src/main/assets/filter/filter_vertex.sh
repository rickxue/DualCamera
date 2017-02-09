attribute vec4 vPosition;
attribute vec4 vTexCoordinate;
//uniform mat4 textureTransform;
// + "attribute vec2 vTexCoordinate;
varying vec2 v_TexCoordinate;
void main() {
    v_TexCoordinate = ( vTexCoordinate).xy;
            // v_TexCoordinate = vTexCoordinate;
    gl_Position = vPosition; 
}
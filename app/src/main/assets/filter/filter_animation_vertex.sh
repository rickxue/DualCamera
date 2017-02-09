attribute vec4 vPosition;
attribute vec4 vTexCoordinate;
varying vec2 v_TexCoordinate;
uniform mat4 uMVPMatrix;

void main()
{
    v_TexCoordinate = ( vTexCoordinate).xy;

    gl_Position = vPosition;
    //gl_Position = uMVPMatrix * vPosition;
    gl_Position = uMVPMatrix * vec4(vPosition.x, vPosition.y, vPosition.z ,1);
}
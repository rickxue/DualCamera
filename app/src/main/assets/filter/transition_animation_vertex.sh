attribute vec4 vPosition;
attribute vec4 vTexCoordinate;
varying vec2 v_TexCoordinate;
void main()
{
    v_TexCoordinate = ( vTexCoordinate).xy;

    gl_Position = vPosition;
}
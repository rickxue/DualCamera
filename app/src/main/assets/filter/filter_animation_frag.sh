precision lowp float;
varying vec2 v_TexCoordinate;
uniform sampler2D uSamplerTex;

void main(void)
{
	vec4 Sample = texture2D(uSamplerTex, v_TexCoordinate);
	gl_FragColor = vec4(Sample.x,Sample.y,Sample.z,1);
}
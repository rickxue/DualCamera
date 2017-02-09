precision lowp float;
varying vec2 v_TexCoordinate;
uniform sampler2D uSamplerTex;
uniform sampler2D uSamplerTex_Blend;
uniform float uT;

void main(void)
{
	//vec4 color1 = texture2D(uSamplerTex, v_TexCoordinate);
    vec4 color2 = texture2D(uSamplerTex_Blend, v_TexCoordinate);
    //gl_FragColor = color2*(1.0-uT) + color1*uT;
    //gl_FragColor = color2*(1.0-uT) + vec4(0.0f,0.0f,0.0f,1.0f)*uT;
    gl_FragColor = color2*(1.0-uT);
}
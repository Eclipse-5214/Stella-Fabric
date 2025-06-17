#version 120
// https://github.com/BiscuitDevelopment/SkyblockAddons/blob/main/src/main/resources/assets/skyblockaddons/shaders/program/chroma_screen.vsh

varying vec4 outColor;

void main(){
  gl_Position=gl_ModelViewProjectionMatrix*gl_Vertex;
  
  // Pass the color & texture coords to the fragment shader
  outColor=gl_Color;
}
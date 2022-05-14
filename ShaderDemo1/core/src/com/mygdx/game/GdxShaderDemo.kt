package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.files.FileHandle
import com.badlogic.gdx.graphics.*
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.utils.NumberUtils.intBitsToFloat
import com.badlogic.gdx.utils.ScreenUtils;
import java.nio.ByteBuffer
import java.nio.ByteOrder

public class GdxShaderDemo: ApplicationAdapter {
	lateinit var batch: SpriteBatch
	lateinit var img: Texture
	lateinit var shaderProgram: ShaderProgram
	lateinit var mesh: Mesh
	lateinit var camera: Camera
	var vertices = FloatArray(6 * 4)
	var overlay = Color(1.0f, 1.0f, 1.0f, 1.0f)
	var fade = 1.0f
	var totalTime = 0.0f

	constructor(): super()

	fun normalizedFloatToByte(f: Float): Int {
		return clamp((f * 255.0f).toInt(), 0, 255)
	}

	fun setVerticiesFromParams(t: Texture, fade: Float, overlay: Color) {
		val overlayAttr = overlay.toFloatBits()
		var i = 0

		// Lower left vertex
		vertices[i++] = 0.0f
		vertices[i++] = 0.0f
		vertices[i++] = 0.0f
		vertices[i++] = 1.0f
		vertices[i++] = fade
		vertices[i++] = overlayAttr

		// Upper left vertex
		vertices[i++] = 0.0f
		vertices[i++] = t.height.toFloat()
		vertices[i++] = 0.0f
		vertices[i++] = 0.0f
		vertices[i++] = fade
		vertices[i++] = overlayAttr

		// Lower right vertex
		vertices[i++] = t.width.toFloat()
		vertices[i++] = 0.0f
		vertices[i++] = 1.0f
		vertices[i++] = 1.0f
		vertices[i++] = fade
		vertices[i++] = overlayAttr

		// Upper right vertex
		vertices[i++] = t.width.toFloat()
		vertices[i++] = t.height.toFloat()
		vertices[i++] = 1.0f
		vertices[i++] = 0.0f
		vertices[i++] = fade
		@Suppress("UNUSED_CHANGED_VALUE")
		vertices[i++] = overlayAttr
	}

	override fun create() {
		batch = SpriteBatch()
		img = Texture("badlogic.jpg")
		camera = OrthographicCamera(600.0f, 600.0f)

		var vShader = FileHandle("shaders/vshader.vert").readString()!!
		var fShader = FileHandle("shaders/fshader.frag").readString()!!
		shaderProgram = ShaderProgram(vShader, fShader)

		mesh = Mesh(true, 4, 0,
		  VertexAttribute(VertexAttributes.Usage.Position, 2, ShaderProgram.POSITION_ATTRIBUTE),
		  VertexAttribute(VertexAttributes.Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE),
		  VertexAttribute(VertexAttributes.Usage.Generic, 1, "a_fade"),
		  VertexAttribute(VertexAttributes.Usage.ColorPacked, 4, "a_overlay"))

		Gdx.gl20.glEnable(GL20.GL_BLEND)
		Gdx.gl20.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA)
	}

	override fun render() {
		ScreenUtils.clear(1.0f, 0.0f, 0.0f, 1.0f)
		setVerticiesFromParams(img, fade, overlay)

		totalTime += Gdx.graphics.deltaTime
		fade = (cos(totalTime) + 1) / 2
		overlay.a = (sin(totalTime) + 1) / 2

		img.bind()
		shaderProgram.bind()
		shaderProgram.setUniformMatrix("u_projTrans", camera.combined)
		shaderProgram.setUniformi("u_texture", 0)
		mesh.setVertices(vertices)
		mesh.render(shaderProgram, GL20.GL_TRIANGLE_STRIP)
	}
	
	@Override
	override fun dispose() {
		batch.dispose()
		img.dispose()
		mesh.dispose()
		shaderProgram.dispose()
	}
}

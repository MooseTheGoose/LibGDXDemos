package com.mygdx.game;

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.Input
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.OrthographicCamera
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.glutils.ShapeRenderer
import com.badlogic.gdx.math.MathUtils.*
import com.badlogic.gdx.math.Rectangle
import com.badlogic.gdx.math.Vector2
import com.badlogic.gdx.utils.Align
import com.badlogic.gdx.utils.ScreenUtils
import kotlin.math.abs
import kotlin.math.sign

val BAT_SIZE = Vector2(16.0f, 64.0f)
val CAMERA_SIZE = Vector2(600.0f, 300.0f)
val BALL_SIZE = Vector2(24.0f, 16.0f)
const val BAT_PADDING = 16.0f
const val BAT_SPEED = 256.0f
val TEXT_PADDING = Vector2(50.0f, 10.0f)
val BALL_START_VEL = 300.0f
val BALL_MAX_MAGNITUDE = 1000.0f
val BALL_VEL_MULT = 0.10f
val BALL_START_POSITION = Vector2(CAMERA_SIZE.x / 2 - BALL_SIZE.x / 2, CAMERA_SIZE.y / 2 - BALL_SIZE.y / 2)

class GdxPongDemo: ApplicationAdapter {

	constructor(): super()

	lateinit var gameCamera: OrthographicCamera
	lateinit var entityRenderer: ShapeRenderer
	lateinit var textRenderer: SpriteBatch
	lateinit var textFont: BitmapFont
	lateinit var input: Input

	var ballRect = Rectangle(BALL_START_POSITION.x, BALL_START_POSITION.y, BALL_SIZE.x, BALL_SIZE.y)
	var ballVel = Vector2()
	var ballWaitForInput = true

	var p1Score = 0
	var p2Score = 0
	var p1Controls = Pair(Input.Keys.W, Input.Keys.S)
	var p2Controls = Pair(Input.Keys.UP, Input.Keys.DOWN)
	var p1Rect = Rectangle(BAT_PADDING, CAMERA_SIZE.y / 2 - BAT_SIZE.y / 2, BAT_SIZE.x, BAT_SIZE.y)
	var p2Rect = Rectangle(CAMERA_SIZE.x - BAT_PADDING - BAT_SIZE.x, p1Rect.y, BAT_SIZE.x, BAT_SIZE.y)


	var deltaTime = 0.0f

	override fun create () {
		input = Gdx.input
		textRenderer = SpriteBatch()
		textFont = BitmapFont()
		entityRenderer = ShapeRenderer()
		gameCamera = OrthographicCamera(CAMERA_SIZE.x, CAMERA_SIZE.y)
		gameCamera.translate(CAMERA_SIZE.x / 2, CAMERA_SIZE.y / 2)
		gameCamera.update()
	}

	fun updatePlayer(pos: Rectangle, keys: Pair<Int, Int>) {
		if(input.isKeyPressed(keys.first))
			pos.y += BAT_SPEED * deltaTime
		else if(input.isKeyPressed(keys.second))
			pos.y -= BAT_SPEED * deltaTime

		val pLowBound = 0.0f
		val pHighBound = CAMERA_SIZE.y - BAT_SIZE.y
		if(pos.y > pHighBound)
			pos.y = pHighBound
		else if(pos.y < pLowBound)
			pos.y = pLowBound
	}

	fun impactFactor(): Float {
		return -(1.0f + BALL_VEL_MULT * random())
	}

	fun updateBall() {
		if(ballWaitForInput) {
			var goLeft = true
			if(input.isKeyPressed(Input.Keys.A) || input.isKeyPressed(Input.Keys.LEFT)) {
				ballWaitForInput = false
			} else if(input.isKeyPressed(Input.Keys.D) || input.isKeyPressed(Input.Keys.RIGHT)) {
				goLeft = false
				ballWaitForInput = false
			}
			if(!ballWaitForInput) {
				var angle = random() * PI / 2 - PI / 4
				if(goLeft)
					angle = PI - angle
				ballVel.x = BALL_START_VEL * cos(angle)
				ballVel.y = BALL_START_VEL * sin(angle)
			}
		}

		if(p1Rect.overlaps(ballRect)) {
			ballRect.x = p1Rect.x + p1Rect.width + 0.1f
			ballVel.x *= impactFactor()
		} else if(p2Rect.overlaps(ballRect)) {
			ballRect.x = p2Rect.x - ballRect.width - 0.1f
			ballVel.x *= impactFactor()
		}

		val yLimit = CAMERA_SIZE.y - ballRect.height
		if(ballRect.y < 0.0f) {
			ballRect.y = 0.0f
			ballVel.y *= impactFactor()
		} else if(ballRect.y > yLimit) {
			ballRect.y = yLimit
			ballVel.y *= impactFactor()
		}

		if(ballRect.x < 0.0f || ballRect.x > CAMERA_SIZE.x) {
			if(ballRect.x < 0.0f)
				p1Score++
			else
				p2Score++
			ballWaitForInput = true
			ballRect.x = BALL_START_POSITION.x
			ballRect.y = BALL_START_POSITION.y
			ballVel.x = 0.0f
			ballVel.y = 0.0f
		}

		if(abs(ballVel.y) > abs(ballVel.x * 5.0f))
			ballVel.x = sign(ballVel.x) * abs(ballVel.y / 5.0f)
		else if(abs(ballVel.x) > abs(ballVel.y * 5.0f))
			ballVel.y = sign(ballVel.y) * abs(ballVel.x / 5.0f)
		val velMag = ballVel.len()
		if(velMag > BALL_MAX_MAGNITUDE) {
			val factor = BALL_MAX_MAGNITUDE / velMag
			ballVel.x *= factor
			ballVel.y *= factor
		}
		ballRect.x += ballVel.x * deltaTime
		ballRect.y += ballVel.y * deltaTime
	}

	override fun render () {
		ScreenUtils.clear(0.0f, 0.0f, 0.0f, 0.0f)
		deltaTime = Gdx.graphics.deltaTime


		updatePlayer(p1Rect, p1Controls)
		updatePlayer(p2Rect, p2Controls)
		updateBall()

		entityRenderer.begin(ShapeRenderer.ShapeType.Filled)
		entityRenderer.projectionMatrix = gameCamera.combined
		entityRenderer.color = Color.WHITE
		entityRenderer.rect(p1Rect.x, p1Rect.y, BAT_SIZE.x, BAT_SIZE.y)
		entityRenderer.rect(p2Rect.x, p2Rect.y, BAT_SIZE.x, BAT_SIZE.y)
		entityRenderer.rect(ballRect.x, ballRect.y, BALL_SIZE.x, BALL_SIZE.y)
		entityRenderer.end()
		textRenderer.begin()
		textRenderer.projectionMatrix = gameCamera.combined
		textFont.draw(textRenderer, p1Score.toString(), TEXT_PADDING.x, CAMERA_SIZE.y - TEXT_PADDING.y, CAMERA_SIZE.x - 2 * TEXT_PADDING.x, Align.left, false)
		textFont.draw(textRenderer, p2Score.toString(), TEXT_PADDING.x, CAMERA_SIZE.y - TEXT_PADDING.y, CAMERA_SIZE.x - 2 * TEXT_PADDING.x, Align.right, false)
		textRenderer.end()
	}

	override fun dispose () {
		entityRenderer.dispose()
		textRenderer.dispose()
	}
}

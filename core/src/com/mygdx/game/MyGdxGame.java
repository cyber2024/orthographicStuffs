package com.mygdx.game;

import com.badlogic.gdx.ApplicationListener;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.ParticleEffect;
import com.badlogic.gdx.graphics.g2d.ParticleEffectPool;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.viewport.FitViewport;
import com.badlogic.gdx.utils.viewport.Viewport;

public class MyGdxGame implements ApplicationListener {
	private static final float
			CAMERA_SPEED = 2.0f,
			CAMERA_ZOOM_SPEED = 2.0f,
			CAMERA_ZOOM_MAX = 1.0f,
			CAMERA_ZOOM_MIN = 0.01f,
			CAMERA_MOVE_EDGE = 0.2f,
			SCENE_WIDTH = 12.8f,
			SCENE_HEIGHT = 7.2f,
			WORLD_TO_SCREEN = 1f/100f,
			HALF_WIDTH = SCENE_WIDTH*0.5f,
			HALF_HEIGHT = SCENE_HEIGHT*0.5f;

	private ParticleEffectPool pool;
	private Array<ParticleEffectPool.PooledEffect> activeEffects;


	private boolean drawGrid = true,
			drawFunction,
			drawCircles,
			drawRectangles,
			drawPoints,
			drawTriangle,
			drawDebug = true;

	private float debugFunction[];


	private OrthographicCamera camera, cameraHUD;
	private Viewport viewport, viewportHUD;
	private SpriteBatch batch;
	private Texture levelTexture;
	private Vector3 touch;
	private ShapeRenderer shapeRenderer;
	
	@Override
	public void create () {
		ParticleEffect explosionEffect = new ParticleEffect();
		explosionEffect.load(Gdx.files.internal("particle-effects/Splosion180.particle"),
				Gdx.files.internal("particle-effects"));
		pool = new ParticleEffectPool(explosionEffect, 10, 100);
		activeEffects = new Array<ParticleEffectPool.PooledEffect>();

		camera= new OrthographicCamera();
		cameraHUD= new OrthographicCamera();
		viewport = new FitViewport(SCENE_WIDTH, SCENE_HEIGHT, camera);
		viewportHUD = new FitViewport(SCENE_WIDTH, SCENE_HEIGHT, cameraHUD);
		batch = new SpriteBatch();
		shapeRenderer = new ShapeRenderer();
		levelTexture = new Texture(Gdx.files.internal("images/jungle-level.png"));
		levelTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
		touch = new Vector3();
		camera.position.x = SCENE_WIDTH*0.5f;
		camera.position.y = SCENE_HEIGHT*0.5f;
		camera.update();
		cameraHUD.update();

		debugFunction = new float[40];

		for(int x = -10 ; x < 10; x++){
			int i = (x+10)*2;
			debugFunction[i] = x;
			debugFunction[i+1] = x*x;
		}
	}

	@Override
	public void resize(int width, int height) {
		viewport.update(width, height);
	}

	@Override
	public void render () {
		Gdx.gl.glClearColor(0,0,0,1);
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		float deltaTime = Gdx.graphics.getDeltaTime();

		if(Gdx.input.isKeyPressed(Input.Keys.LEFT)){
			camera.position.x -= deltaTime*CAMERA_SPEED;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.RIGHT)){
			camera.position.x += deltaTime*CAMERA_SPEED;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.UP)){
			camera.position.y -= deltaTime*CAMERA_SPEED;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.DOWN)){
			camera.position.y += deltaTime*CAMERA_SPEED;
		}
		if(Gdx.input.isTouched()){
			touch.set(Gdx.input.getX(), Gdx.input.getY(), 0f);
			viewport.unproject(touch);

			if(touch.x > camera.position.x + SCENE_WIDTH*0.5f*(1f - CAMERA_MOVE_EDGE)*camera.zoom){
				camera.position.x += deltaTime*CAMERA_SPEED;
			}
			if(touch.x < (camera.position.x -  SCENE_WIDTH*0.5f*(1-CAMERA_MOVE_EDGE)*camera.zoom)){
				camera.position.x -= deltaTime*CAMERA_SPEED;
			}
			if(touch.y > camera.position.y + SCENE_HEIGHT*0.5f*(1f - CAMERA_MOVE_EDGE)*camera.zoom){
				camera.position.y += deltaTime*CAMERA_SPEED;
			}
			if(touch.y < camera.position.y - (SCENE_HEIGHT*0.5f*(1f - CAMERA_MOVE_EDGE)*camera.zoom)){
				camera.position.y -= deltaTime*CAMERA_SPEED;
			}

			ParticleEffectPool.PooledEffect effect = pool.obtain();
			if(effect != null){
				activeEffects.add(effect);
				effect.setPosition(touch.x, touch.y);
			}

		}
		if(Gdx.input.isKeyPressed(Input.Keys.PAGE_UP)){
			camera.zoom -= CAMERA_ZOOM_SPEED * deltaTime;
		}
		if(Gdx.input.isKeyPressed(Input.Keys.PAGE_DOWN)){
			camera.zoom += CAMERA_ZOOM_SPEED * deltaTime;
		}
		camera.position.x = MathUtils.clamp(camera.position.x, HALF_WIDTH*camera.zoom,
				levelTexture.getWidth()*WORLD_TO_SCREEN - HALF_WIDTH*camera.zoom);
		camera.position.y = MathUtils.clamp(camera.position.y, HALF_HEIGHT*camera.zoom,
				levelTexture.getHeight()*WORLD_TO_SCREEN - HALF_HEIGHT*camera.zoom);
		camera.zoom = MathUtils.clamp(camera.zoom, CAMERA_ZOOM_MIN, CAMERA_ZOOM_MAX);

		Gdx.app.log("Camera Position: ", camera.position.toString());
		Gdx.app.log("Camera Zoom: ", Float.toString(camera.zoom));
		Gdx.app.log("Touch: ", touch.toString());

		camera.update();
		batch.setProjectionMatrix(camera.combined);

		batch.begin();
		batch.draw(levelTexture,
				0f, 0f,
				0f, 0f,
				levelTexture.getWidth(), levelTexture.getHeight(),
				WORLD_TO_SCREEN, WORLD_TO_SCREEN,
				0f,
				0, 0,
				levelTexture.getWidth(), levelTexture.getHeight(),
				false, false);


			for(int i = 0; i < activeEffects.size; i++){
				ParticleEffectPool.PooledEffect effect;
				effect = activeEffects.get(i);
				if(effect.isComplete()){
					pool.free(effect);
					activeEffects.removeIndex(i);
				} else {
					effect.draw(batch,deltaTime);
				}
			}

		batch.end();

		shapeRenderer.setProjectionMatrix(camera.combined);

		if(drawDebug){
			drawDebug(shapeRenderer);
		}



		cameraHUD.update();
		batch.setProjectionMatrix(cameraHUD.combined);
		batch.begin();

		batch.end();

		if(Gdx.input.isKeyJustPressed(Input.Keys.D)){
			drawDebug = !drawDebug;
		}
		Gdx.app.log("FPS: ", 1/deltaTime + " fps");

	}

	@Override
	public void pause() {

	}

	@Override
	public void resume() {

	}

	@Override
	public void dispose() {
		batch.dispose();
		levelTexture.dispose();
		shapeRenderer.dispose();
		pool.clear();
		activeEffects.clear();

	}

	public void drawDebug(ShapeRenderer shapeRenderer){
		if(drawGrid){
			shapeRenderer.begin(ShapeType.Line);
			shapeRenderer.setColor(Color.BLUE);
			for(int i = 0; i < 10; i++){
				shapeRenderer.line(-SCENE_WIDTH, 0.1f * SCENE_HEIGHT * i, SCENE_WIDTH, 0.1f * SCENE_HEIGHT * i);
				shapeRenderer.line(0.1f * SCENE_WIDTH * i, -SCENE_HEIGHT, 0.1f * SCENE_WIDTH * i, SCENE_HEIGHT);
			}
			shapeRenderer.setColor(Color.RED);
			shapeRenderer.line(-SCENE_WIDTH, 0.5f * SCENE_HEIGHT, SCENE_WIDTH, 0.5f * SCENE_HEIGHT);
			shapeRenderer.line(0.5f * SCENE_WIDTH, -SCENE_HEIGHT, 0.5f * SCENE_WIDTH, SCENE_HEIGHT);

			shapeRenderer.end();
		}

	}
}

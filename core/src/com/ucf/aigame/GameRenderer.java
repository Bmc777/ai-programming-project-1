package com.ucf.aigame;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;

/**
 * Created by Bryan on 1/21/2016.
 */
public class GameRenderer
{
    private GameWorld gameWorld;
    private OrthographicCamera camera;
    private SpriteBatch batcher;
    private ShapeRenderer shapeRenderer;
    private BitmapFont bitmapFont;
    private Debugger debugger;

    private TextureRegion playerEntityTextureRegion;
    private TextureRegion gameEntityTextureRegion;
    private Texture floorTileTexture;
    private Texture wallTileTexture;

    private PlayerEntity playerEntity;
    private GameEntity gameEntity1;
    private GameEntity gameEntity2;

    private float gameWidth;
    private float gameHeight;

    private static final float TILE_DIMENSIONS = 32;

    private float tempFloat = 0;

    public GameRenderer(GameWorld gameWorld, Debugger debugger, float gameWidth, float gameHeight)
    {
        this.gameWorld = gameWorld;
        this.debugger = debugger;
        this.gameWidth = gameWidth;
        this.gameHeight = gameHeight;

        //Sets Camera to Orthographic for 2D view of the screen.
        camera = new OrthographicCamera();
        //false so y increases as it goes up instead of down, bottom left corner is coordinate (0,0)
        camera.setToOrtho(false, gameWidth, gameHeight);

        //Telling SpriteBatch and shapeRenderer to use camera's coordinates when drawing Sprites
        batcher = new SpriteBatch();
        batcher.setProjectionMatrix(camera.combined);

        shapeRenderer = new ShapeRenderer();
        shapeRenderer.setProjectionMatrix(camera.combined);

        bitmapFont = new BitmapFont();

        initializeGameAssets();
        initializeAssets();
    }

    public void render(float runTime)
    {
        //OpenGL graphics stuff
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        renderBackground();

        renderPlayerEntity();
        renderGameEntities();

        if (debugger.getDebugDisplayState())
        {
            Gdx.gl.glEnable(GL20.GL_BLEND);
            Gdx.gl.glBlendFunc(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

            renderWallSensor();
            renderAdjacentAgentSensors();
            renderPieSliceSensor();

            renderPlayerInformation();


            Gdx.gl.glDisable(GL20.GL_BLEND);
        }

        renderDebugState();
    }

    private void renderBackground()
    {
        batcher.begin();

        //Fills the screen with floor and wall tiles.
        for (int x = 0; x < gameWidth; x += TILE_DIMENSIONS)
        {
            for (int y = 0; y < gameHeight; y += TILE_DIMENSIONS)
            {
                if (x == 0 || x == gameWidth - TILE_DIMENSIONS || y == 0 || y == gameHeight - TILE_DIMENSIONS)
                {
                    batcher.draw(wallTileTexture, x, y);
                }
                else
                {
                    batcher.draw(floorTileTexture, x, y);
                }
            }
        }


        batcher.end();
    }

    private void renderPlayerEntity()
    {
        batcher.begin();

        //Drawing the playerEntityTexture
        batcher.draw(playerEntityTextureRegion, playerEntity.getCurrentXPosition(), playerEntity.getCurrentYPosition(),
                playerEntity.getXPlayerOrigin(), playerEntity.getYPlayerOrigin(), playerEntity.getWidth(), playerEntity.getHeight(),
                1, 1, playerEntity.getRotationAngle());

        batcher.end();
    }

    private void renderPlayerInformation()
    {
        batcher.begin();
        bitmapFont.setColor(255f, 255f, 255f, 1);

        bitmapFont.draw(batcher, "Heading: " + playerEntity.getCurrentHeading().toString(), playerEntity.getCurrentXPosition() -16, playerEntity.getCurrentYPosition());
        bitmapFont.draw(batcher, "Position:  (" + Float.toString(playerEntity.getCurrentXPosition()) + ", " +
                Float.toString(playerEntity.getCurrentYPosition()) + ")", playerEntity.getCurrentXPosition() - 16, playerEntity.getCurrentYPosition() - 16);

        batcher.end();
    }

    private void renderGameEntities()
    {
        batcher.begin();

        batcher.draw(gameEntityTextureRegion, gameEntity1.getCurrentXPosition(), gameEntity1.getCurrentYPosition(),
                gameEntity1.getXEntityOrigin(), gameEntity1.getYEntityOrigin(), gameEntity1.getWidth(), gameEntity1.getHeight(),
                1, 1, gameEntity1.getRotationAngle());

        batcher.draw(gameEntityTextureRegion, gameEntity2.getCurrentXPosition(), gameEntity2.getCurrentYPosition(),
                gameEntity2.getXEntityOrigin(), gameEntity2.getYEntityOrigin(), gameEntity2.getWidth(), gameEntity2.getHeight(),
                1, 1, gameEntity2.getRotationAngle());

        batcher.end();
    }

    private void renderWallSensor()
    {
        int lineHeightTextOffset = 0;

        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(gameWidth - 32 * 8, 32, 32 * 7, 32 * 3);
        shapeRenderer.setColor(0/255f, 34/255f, 255/255f, 0.5f);
        shapeRenderer.rect((gameWidth + 2) - 32 * 8,2 + 32, (32 * 7) - 4, (32 * 3) - 4);
        shapeRenderer.end();

        for (int i = 0; i < playerEntity.getWallSensorArray().length; i++)
        {
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

            shapeRenderer.setColor(255/255f, 180/255f, 0, 0.5f);

            shapeRenderer.rectLine(playerEntity.getWallSensorOriginX(), playerEntity.getWallSensorOriginY(),
                    playerEntity.getWallSensorEndpointX(i), playerEntity.getWallSensorEndpointY(i), 1);

            shapeRenderer.circle(playerEntity.getWallSensorEndpointX(i), playerEntity.getWallSensorEndpointY(i), 4);

            shapeRenderer.end();

            batcher.begin();

            bitmapFont.setColor(255/255f, 180/255f, 0, 1);

            bitmapFont.draw(batcher, Integer.toString(i), playerEntity.getWallSensorEndpointX(i) + 8, playerEntity.getWallSensorEndpointY(i) + 8);
            bitmapFont.draw(batcher, "Wall Sensor " + Integer.toString(i) + " Length: "
                    + Float.toString(debugger.getWallSensorLengthOutput(i)), (gameWidth + 4) - 32 * 8, 32 * 3.5f + 6 - lineHeightTextOffset);


            lineHeightTextOffset += 16;
            batcher.end();
        }
    }

    private void renderAdjacentAgentSensors()
    {
        // For AdjacentAgentSensor
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // Draw AdjacentAgentSensor Circle
        shapeRenderer.circle(playerEntity.getAdjecentAgentSensor().x, playerEntity.getAdjecentAgentSensor().y,
                playerEntity.getAdjecentAgentSensor().radius);

        // Check all GameEntities
        for (int i = 0; i < gameWorld.getEntityList().size(); i++) {

            // Check if detected by AdjacentAgentSensor
            if ( gameWorld.getEntityList().get(i).isDetected() ) {
                shapeRenderer.setColor(255, 0, 0, 0.5f);

                // Draw Circle
                shapeRenderer.circle(gameWorld.getEntityList().get(i).getEntityCenter().x,
                        gameWorld.getEntityList().get(i).getEntityCenter().y,
                        gameWorld.getEntityList().get(i).getWidth());

                // Draw Relative Heading
                shapeRenderer.rectLine(playerEntity.getvOrigin(), gameWorld.getEntityList().get(i).getEntityCenter(), 1);
            }
        }

        shapeRenderer.end();
    }

    private void renderPieSliceSensor()
    {
        for (int i = 0; i < gameWorld.getEntityList().size(); i++) {

            // Check if detected by AdjacentAgentSensor
            if ( gameWorld.getEntityList().get(i).isDetected() ) {
                shapeRenderer.setColor(255, 0, 0, 0.5f);

                // For: PieSliceSensor (detected)
                // Identifies Quadrant and increments its Activation Level
                playerEntity.getPieSliceSensor().identifyQuadrant(
                        playerEntity.getCurrentHeading(),
                        gameWorld.getEntityList().get(i).getEntityCenter().sub(playerEntity.getvOrigin()));
            }
        }
        // Activation Levels have been tallied, Now get highest Activation Level and draw / color lines

        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);

        // FRONT RIGHT
        tempFloat = Math.max(playerEntity.getPieSliceSensor().getActivationLevelFRONT(), playerEntity.getPieSliceSensor().getActivationLevelRIGHT());
        if (tempFloat < 1) { // Green
            shapeRenderer.setColor(0, 255, 0, 0.5f);
        }
        else if (tempFloat == 1) { // Yellow
            shapeRenderer.setColor(255, 255, 0, 0.5f);
        }
        else { // Red
            shapeRenderer.setColor(255, 0, 0, 0.5f);
        }
        shapeRenderer.rectLine(playerEntity.getvOrigin(),
                playerEntity.getPieSliceSensor().getFrontRight().add(playerEntity.getvOrigin()), 0.5f);

        // FRONT LEFT
        tempFloat = Math.max(playerEntity.getPieSliceSensor().getActivationLevelFRONT(), playerEntity.getPieSliceSensor().getActivationLevelLEFT());
        if (tempFloat < 1) { // Green
            shapeRenderer.setColor(0, 255, 0, 1);
        }
        else if (tempFloat == 1) { // Yellow
            shapeRenderer.setColor(255, 255, 0, 1);
        }
        else { // Red
            shapeRenderer.setColor(255, 0, 0, 1);
        }
        shapeRenderer.rectLine(playerEntity.getvOrigin(),
                playerEntity.getPieSliceSensor().getFrontLeft().add(playerEntity.getvOrigin()), 0.5f);

        // BACK LEFT
        tempFloat = Math.max(playerEntity.getPieSliceSensor().getActivationLevelBACK(), playerEntity.getPieSliceSensor().getActivationLevelLEFT());
        if (tempFloat < 1) { // Green
            shapeRenderer.setColor(0, 255, 0, 0.5f);
        }
        else if (tempFloat == 1) { // Yellow
            shapeRenderer.setColor(255, 255, 0, 0.5f);
        }
        else { // Red
            shapeRenderer.setColor(255, 0, 0, 0.5f);
        }
        shapeRenderer.rectLine(playerEntity.getvOrigin(),
                playerEntity.getPieSliceSensor().getBackLeft().add(playerEntity.getvOrigin()), 0.5f);

        // BACK RIGHT
        tempFloat = Math.max(playerEntity.getPieSliceSensor().getActivationLevelBACK(), playerEntity.getPieSliceSensor().getActivationLevelRIGHT());
        if (tempFloat < 1) { // Green
            shapeRenderer.setColor(0, 255, 0, 0.5f);
        }
        else if (tempFloat == 1) { // Yellow
            shapeRenderer.setColor(255, 255, 0, 0.5f);
        }
        else { // Red
            shapeRenderer.setColor(255, 0, 0, 0.5f);
        }
        shapeRenderer.rectLine(playerEntity.getvOrigin(),
                playerEntity.getPieSliceSensor().getBackRight().add(playerEntity.getvOrigin()), 0.5f);


        // Reset Color to White and Reset PieSliceSensors
        shapeRenderer.setColor(255, 255, 255, 0.5f);
        playerEntity.getPieSliceSensor().resetActivationLevels();

        shapeRenderer.end();
    }

    private void renderDebugState()
    {
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        shapeRenderer.setColor(0, 0, 0, 0.5f);
        shapeRenderer.rect(0, 0, 128, 24);
        shapeRenderer.setColor(0/255f, 34/255f, 255/255f, 0.5f);
        shapeRenderer.rect(2, 2, 124, 20);

        shapeRenderer.end();

        batcher.begin();

        if (debugger.getDebugDisplayState())
        {
            bitmapFont.setColor(0/255f, 255/255f, 43/255f, 1);
            bitmapFont.draw(batcher, "[V] Debugger ON", 6, 18);
        }
        else
        {
            bitmapFont.setColor(255/255f, 0/255f, 0/255f, 1);
            bitmapFont.draw(batcher, "[V] Debugger OFF", 6, 18);
        }

        batcher.end();
    }

    private void initializeGameAssets()
    {
        playerEntity = gameWorld.getPlayerEntity();
        gameEntity1 = gameWorld.getGameEntity1();
        gameEntity2 = gameWorld.getGameEntity2();
    }

    private void initializeAssets()
    {
        playerEntityTextureRegion = AssetLoader.playerEntityTextureRegion;
        gameEntityTextureRegion = AssetLoader.gameEntityTextureRegion;
        wallTileTexture = AssetLoader.wallTileTexture;
        floorTileTexture = AssetLoader.floorTileTexture;
    }
}

package com.bitfire.uracer.game.actors;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.BodyDef;
import com.badlogic.gdx.physics.box2d.BodyDef.BodyType;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.FixtureDef;
import com.badlogic.gdx.physics.box2d.MassData;
import com.bitfire.uracer.configuration.Config;
import com.bitfire.uracer.game.collisions.CollisionFilters;
import com.bitfire.uracer.game.rendering.GameRendererEvent;
import com.bitfire.uracer.game.world.GameWorld;
import com.bitfire.uracer.resources.Art;
import com.bitfire.uracer.utils.AMath;
import com.bitfire.uracer.utils.Convert;
import com.bitfire.uracer.utils.FixtureAtlas;

public abstract class Car extends Box2DEntity {
	public enum InputMode {
		NoInput, InputFromPlayer, InputFromReplay
	}

	public enum Aspect {
		OldSkool( "electron" ), OldSkool2( "spider" );

		public final String name;

		private Aspect( String name ) {
			this.name = name;
		}
	}

	/* event */
	public CarEvent event = null;
	private boolean triggerEvents = false;

	protected GameWorld gameWorld;
	protected CarModel model = new CarModel();
	protected CarType carType;
	protected CarRenderer renderer;

	protected int impacts = 0;

	private CarForces carForces = new CarForces();

	// distance
	private Vector2 previousPosition = new Vector2();
	private Vector2 distmp = new Vector2();

	// the car's traveled distance so far, in meters
	private float carTraveledDistance = 0;
	private int accuDistCount = 0;

	// the car's average speed, in meters/sec, so far.
	private float carAvgSpeedMtSec = 0;
	private float accuSpeed = 0;
	private int accuSpeedCount = 0;

	// the car's instant speed, in meters/sec
	private float carInstantSpeedMtSec = 0;

	private Aspect aspect = Aspect.OldSkool;
	protected InputMode inputMode = InputMode.NoInput;

	public Car( GameWorld gameWorld, CarType carType, InputMode inputMode, GameRendererEvent.Order drawingOrder, CarModel model, Aspect aspect, boolean triggerEvents ) {
		super( gameWorld.getBox2DWorld(), drawingOrder );
		this.aspect = aspect;
		this.model.set( model );
		this.carType = carType;
		this.triggerEvents = triggerEvents;

		this.event = new CarEvent( this );
		this.gameWorld = gameWorld;
		this.renderer = new CarRenderer( model, aspect );
		this.impacts = 0;
		this.inputMode = inputMode;
		this.carTraveledDistance = 0;
		this.accuDistCount = 0;
		this.accuSpeed = 0;
		this.accuSpeedCount = 0;

		applyCarPhysics( carType );

		Gdx.app.log( getClass().getSimpleName(), "Input mode is " + this.inputMode.toString() );
		Gdx.app.log( getClass().getSimpleName(), "CarModel is " + this.model.type.toString() );
	}

	private void applyCarPhysics( CarType carType ) {
		if( body != null ) {
			this.box2dWorld.destroyBody( body );
		}

		// body
		BodyDef bd = new BodyDef();
		bd.angle = 0;
		bd.type = BodyType.DynamicBody;

		body = box2dWorld.createBody( bd );
		body.setBullet( true );
		body.setUserData( this );

		TextureRegion region = Art.cars.findRegion( aspect.name );
		String shapeName = Config.ShapesStore + "electron" /* aspect.name */+ ".shape";
		String shapeRef = Config.ShapesRefs + "electron" /* aspect.name */+ ".png";

		// set physical properties and apply shape
		FixtureDef fd = new FixtureDef();
		fd.density = model.density;
		fd.friction = model.friction;
		fd.restitution = model.restitution;

		fd.filter.groupIndex = (short)((carType == CarType.PlayerCar) ? CollisionFilters.GroupPlayer : CollisionFilters.GroupReplay);
		fd.filter.categoryBits = (short)((carType == CarType.PlayerCar) ? CollisionFilters.CategoryPlayer : CollisionFilters.CategoryReplay);
		fd.filter.maskBits = (short)((carType == CarType.PlayerCar) ? CollisionFilters.MaskPlayer : CollisionFilters.MaskReplay);

		if( Config.Debug.TraverseWalls ) {
			fd.filter.groupIndex = CollisionFilters.GroupNoCollisions;
		}

		// apply scaling factors
		Vector2 offset = new Vector2( -model.width / 2f, -model.length / 2f );

		Vector2 ratio = new Vector2( model.width / Convert.px2mt( region.getRegionWidth() ), model.length / Convert.px2mt( region.getRegionHeight() ) );

		// box2d editor "normalization" contemplates just a width-bound ratio..
		Vector2 factor = new Vector2( Convert.px2mt( region.getRegionWidth() * ratio.x ), Convert.px2mt( region.getRegionWidth() * ratio.y ) );

		FixtureAtlas atlas = new FixtureAtlas( Gdx.files.internal( shapeName ) );
		atlas.createFixtures( body, shapeRef, factor.x, factor.y, fd, offset, carType );

		// dbg
//		FixtureDef fd = new FixtureDef();
//		Vector2 p = new Vector2();
//		CircleShape shape = new CircleShape();
//		shape.setPosition( p.set(0,0.75f) );
//		shape.setRadius( 2.5f / 2f );
//		fd.shape = shape;
//
//		fd.density = 1;
//		fd.friction = 2f;
//		fd.restitution = 0f;
//		fd.filter.groupIndex = (short)((carType == CarType.PlayerCar) ? CollisionFilters.GroupPlayer : CollisionFilters.GroupReplay);
//		fd.filter.categoryBits = (short)((carType == CarType.PlayerCar) ? CollisionFilters.CategoryPlayer : CollisionFilters.CategoryReplay);
//		fd.filter.maskBits = (short)((carType == CarType.PlayerCar) ? CollisionFilters.MaskPlayer : CollisionFilters.MaskReplay);
//
//		body.createFixture( fd ).setUserData( carType );
//		shape.setPosition( p.set(0,-0.75f) );
//		body.createFixture( fd ).setUserData( carType );
		// dbg


		MassData mdata = body.getMassData();
		mdata.center.set( 0, 0 );
		body.setMassData( mdata );
	}

	/** Subclasses will feed forces to the simulator, such as Replay data stored
	 * elsewhere or from user input.
	 *
	 * @param forces computed forces shall be returned by filling the passed data structure. */
	protected abstract void onComputeCarForces( CarForces forces );

	public Aspect getAspect() {
		return aspect;
	}

	public CarModel getCarModel() {
		return model;
	}

	public InputMode getInputMode() {
		return inputMode;
	}

	public CarRenderer getRenderer() {
		return renderer;
	}

	public void setAspect( Aspect aspect ) {
		if( this.aspect != aspect ) {
			this.aspect = aspect;
			renderer.setAspect( aspect, model );
			Gdx.app.log( this.getClass().getSimpleName(), "Switched to car aspect type \"" + aspect.toString() + "\"" );
		}
	}

	public void setCarModel( CarModel.Type modelType ) {
		if( model.type != modelType ) {
			model.toModelType( modelType );
			applyCarPhysics( carType );
			renderer.setAspect( aspect, model );
			Gdx.app.log( this.getClass().getSimpleName(), "Switched to car model \"" + model.type.toString() + "\"" );
		}
	}

	/** Returns the traveled distance, in meters, so far.
	 * Calling reset() will also reset the traveled distance. */
	public float getTraveledDistance() {
		return carTraveledDistance;
	}

	/** Returns the average speed, in meters/s, so far. */
	public float getAverageSpeed() {
		return carAvgSpeedMtSec;
	}

	/** Returns the instant speed, in meters/s */
	public float getInstantSpeed() {
		return carInstantSpeedMtSec;
	}

	public int getAccuDistCount() {
		return accuDistCount;
	}

	public int getAccuSpeedCount() {
		return accuSpeedCount;
	}

	public void setActive( boolean active ) {
		if( active != body.isActive() ) {
			body.setActive( active );
		}
	}

	public boolean isActive() {
		return body.isActive();
	}

	public void reset() {
		resetPhysics();
		resetTraveledDistance();
	}

	public void resetTraveledDistance() {
		carTraveledDistance = 0;
		accuDistCount = 0;
		accuSpeed = 0;
		accuSpeedCount = 0;
	}

	protected void resetPhysics() {
		boolean wasActive = isActive();

		if( wasActive ) {
			body.setActive( false );
		}

		body.setAngularVelocity( 0 );
		body.setLinearVelocity( 0, 0 );

		if( wasActive ) {
			body.setActive( wasActive );
		}

		impacts = 0;
	}

	public void onCollide( Fixture other, Vector2 normalImpulses ) {
		impacts++;

//		Gdx.app.log( "", "x=" + NumberString.formatVeryLong(normalImpulses.x) + ", y=" + NumberString.formatVeryLong(normalImpulses.y) );

		if( triggerEvents ) {
			event.data.setCollisionData( other, normalImpulses );
			event.trigger( this, CarEvent.Type.onCollision );
		}
	}

	@Override
	public void setTransform( Vector2 position, float orient ) {
		super.setTransform( position, orient );
		previousPosition.set( body.getPosition() );
	}

	@Override
	public void setWorldPosMt( Vector2 worldPosition ) {
		super.setWorldPosMt( worldPosition );
		previousPosition.set( body.getPosition() );
	}

	@Override
	public void setWorldPosMt( Vector2 worldPosition, float orientationRads ) {
		super.setWorldPosMt( worldPosition, orientationRads );
		previousPosition.set( body.getPosition() );
	}

	@Override
	public void onBeforePhysicsSubstep() {
		// keeps track of the previous position to be able
		// to compute the cumulative traveled distance
//		previousPosition.set( body.getPosition() );

		super.onBeforePhysicsSubstep();

		// let's subclasses behave as needed, ask them to fill carForces with new data
		onComputeCarForces( carForces );

		// trigger event, new forces have been computed
		if( triggerEvents ) {
			event.data.setForces( carForces );
			event.trigger( this, CarEvent.Type.onComputeForces );
		}

		// FIXME is it really necessary? that's an expensive jni call..
		// body.setAwake( true );

		// put newly computed forces into the system
		body.setLinearVelocity( carForces.velocity_x, carForces.velocity_y );
		body.setAngularVelocity( -carForces.angularVelocity );
	}

	@Override
	public void onAfterPhysicsSubstep() {
		super.onAfterPhysicsSubstep();

		// compute traveled distance, in meters
		distmp.set( body.getPosition() );
		distmp.sub( previousPosition );
		previousPosition.set( body.getPosition() );

		// filter out zero distance
		float dist = AMath.fixup( distmp.len() );

		if( !AMath.isZero( dist ) ) {
//			Gdx.app.log( "", "dist=" + NumberString.formatVeryLong( dist ) );
			// accumulate distance it
			carTraveledDistance += dist;
			accuDistCount++;
		} else {
			dist = 0;
		}

		// compute instant speed
		carInstantSpeedMtSec = AMath.fixup( dist * Config.Physics.PhysicsTimestepHz );
		accuSpeed += carInstantSpeedMtSec;
		accuSpeedCount++;

		// compute average speed
		carAvgSpeedMtSec = AMath.fixup( accuSpeed / accuSpeedCount );
//		Gdx.app.log( this.getClass().getSimpleName(), "avg_speed=" + NumberString.formatVeryLong(carAvgSpeedMtSec) );
	}

	@Override
	public void onRender( SpriteBatch batch ) {
		renderer.render( batch, stateRender );
	}
}
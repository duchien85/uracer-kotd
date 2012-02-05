package com.bitfire.uracer.carsimulation;

import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.physics.box2d.Contact;
import com.badlogic.gdx.physics.box2d.ContactImpulse;
import com.badlogic.gdx.physics.box2d.ContactListener;
import com.badlogic.gdx.physics.box2d.Fixture;
import com.badlogic.gdx.physics.box2d.Manifold;
import com.bitfire.uracer.audio.CarSoundManager;
import com.bitfire.uracer.entities.vehicles.Car;
import com.bitfire.uracer.game.logic.DriftInfo;
import com.bitfire.uracer.utils.Box2DUtils;

public class CarContactListener implements ContactListener
{
	@Override
	public void beginContact( Contact contact )
	{
	}

	@Override
	public void endContact( Contact contact )
	{
	}

	@Override
	public void preSolve( Contact contact, Manifold oldManifold )
	{
	}

	Vector2 tmp = new Vector2();

	private void addImpactFeedback( Fixture f, ContactImpulse impulse )
	{
		if( (Box2DUtils.isCar( f ) || Box2DUtils.isGhostCar( f )) && f.getBody() != null )
		{
			Car car = (Car)f.getBody().getUserData();
			float[] impulses = impulse.getNormalImpulses();
			tmp.set( impulses[0], impulses[1] );

			float res = tmp.len();
			if( res > 0 )
			{
				car.addImpactFeedback( res );

				// update DriftInfo in case of collision
				if( car.getInputMode() == CarInputMode.InputFromPlayer )
				{
//					System.out.println( "Impact data = " + res );

					CarSoundManager.carImpacted( res );
					DriftInfo.invalidateByCollision();
				}
			}
		}
	}

	@Override
	public void postSolve( Contact contact, ContactImpulse impulse )
	{
		Fixture a = contact.getFixtureA();
		Fixture b = contact.getFixtureB();

		addImpactFeedback( a, impulse );
		addImpactFeedback( b, impulse );
	}
}

package com.bitfire.uracer.postprocessing;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Pixmap.Format;
import com.badlogic.gdx.graphics.glutils.FrameBuffer;

public final class PostProcessor
{
	private final FrameBuffer bufferScene;
	private final Format fbFormat;
	private boolean capturing = false;
	private IPostProcessorEffect effect = null;
	private Color clearColor = Color.CLEAR;

	public PostProcessor( int fboWidth, int fboHeight, boolean useDepth, boolean useAlphaChannel, boolean use32Bits)
	{
		if( use32Bits )
		{
			if( useAlphaChannel )
			{
				fbFormat = Format.RGBA8888;
			} else
			{
				fbFormat = Format.RGB888;
			}

		} else
		{
			if( useAlphaChannel )
			{
				fbFormat = Format.RGBA4444;
			} else
			{
				fbFormat = Format.RGB565;
			}
		}

		bufferScene = new FrameBuffer( fbFormat, fboWidth, fboHeight, useDepth );

		capturing = false;
	}

	public void dispose()
	{
		if(effect != null)
			effect.dispose();

		bufferScene.dispose();
	}

	public void setEffect(IPostProcessorEffect effect)
	{
		this.effect = effect;
	}

	public Format getFramebufferFormat()
	{
		return fbFormat;
	}

	public void setClearColor(Color color)
	{
		clearColor.set( color );
	}

	public void setClearColor( float r, float g, float b, float a )
	{
		clearColor.set( r, g, b, a );
	}

	/**
	 * Start capturing the scene
	 */
	public void capture()
	{
		if(!capturing)
		{
			capturing = true;
			bufferScene.begin();

			Gdx.gl.glClearColor( clearColor.r, clearColor.g, clearColor.b, clearColor.a );
			Gdx.gl.glClear( GL10.GL_COLOR_BUFFER_BIT | GL10.GL_DEPTH_BUFFER_BIT );
		}
	}

	/**
	 * Pause capturing
	 */
	public void capturePause()
	{
		if(capturing)
		{
			capturing = false;
			bufferScene.end();
		}
	}

	/**
	 * Start capturing again, after pause
	 */
	public void captureContinue()
	{
		if(!capturing)
		{
			capturing = true;
			bufferScene.begin();
		}
	}

	/**
	 * Stops capturing the scene
	 */
	public void captureEnd()
	{
		if(capturing)
		{
			capturing = false;
			bufferScene.end();
		}
	}

	/**
	 * call this when resuming
	 */
	public void resume()
	{
		if( effect != null )
			effect.resume();
	}

	/**
	 * Finish capturing the scene, post-process and render the effect, if any
	 */
	public void render()
	{
		captureEnd();

		if(effect != null)
		{
			effect.render( bufferScene );
		}
	}
}

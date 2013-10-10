/*******************************************************************************
 * Copyright 2012, The Infinit.e Open Source Project.
 * 
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License, version 3,
 * as published by the Free Software Foundation.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Affero General Public License for more details.
 * 
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 ******************************************************************************/
package com.ikanow.infinit.e.widget.library.components
{
	import com.ikanow.infinit.e.widget.library.assets.skins.WidgetFooterGroupSkin;
	import spark.components.SkinnableContainer;
	
	public class WidgetFooterGroup extends SkinnableContainer
	{
		
		//======================================
		// constructor 
		//======================================
		
		public function WidgetFooterGroup()
		{
			super();
		}
		
		
		//======================================
		// public methods 
		//======================================
		
		/**
		 * Set the default skin class
		 */
		override public function stylesInitialized():void
		{
			super.stylesInitialized();
			this.setStyle( "skinClass", Class( WidgetFooterGroupSkin ) );
			
			if ( isNaN( getStyle( "contentBackgroundColor" ) ) )
				this.setStyle( "contentBackgroundColor", 0xE8E9E9 );
			
			invalidateDisplayList();
		}
	}
}

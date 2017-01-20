/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, { Component, PropTypes } from 'react'
import {Link} from 'react-router'
import Btn from './../components/Btn'

export default class LandingPage extends Component {
	constructor(props) {
		super(props);

		this.state = {};
	}
	render(){
		return (
			<div className="LandingPage">
				<h1>	
					Welcome to Europa Community
				</h1>
				<div className="FlexRow">
					<div className='Flex1'>
						<Btn text="Get Started" 
							 onClick={() => this.context.router.push('/repositories') }
							 style={{width: '480px'}}/>
					</div>
				</div>
			</div>
		);
	}
}

LandingPage.contextTypes = {
	router: PropTypes.object,
	actions: PropTypes.object
};

LandingPage.childContextTypes = {
	actions: PropTypes.object,
	router: PropTypes.object
};





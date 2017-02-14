/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import Btn from './../components/Btn'

export default class NotFound extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	toHome(){
		let redirect = (this.props.hasOwnProperty('storage') && this.props.storage == false) ? '/' : '/repositories';
		this.context.router.push(redirect);
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="NoContent" style={{marginTop: '42px'}}>
					<h3>Page Not Found.</h3>
					<Btn onClick={() => this.toHome()} 
						 className="LargeBlueButton"
						 text="Take Me Home" 
						 canClick={true}/>
				</div>
			</div>

		);
	}
}

NotFound.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

NotFound.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

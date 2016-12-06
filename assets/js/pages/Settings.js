import React, {Component} from 'react'
import Btn from './../components/Btn'
import ErrorMsg from './../components/ErrorMsg'

export default class Settings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Settings
				</h2>
				<div>
					
				</div>
			</div>
		);
	}
}

Settings.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Settings.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
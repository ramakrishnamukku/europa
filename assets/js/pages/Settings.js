import React, {Component} from 'react'
import Registries from './../components/Registries'
import Btn from './../components/Btn'

export default class Settings extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderRegistries(){
		return (
			<Registries />
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Settings
					</h2>
				</div>
				<div className="FlexRow RowPadding">
					<div className="Flex1">
						some shit
					</div>
					<div className="Flex3">
						{this.renderRegistries()}
					</div>
				</div>
			</div>
		);
	}
}

Settings.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

Settings.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
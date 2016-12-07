import React, {Component} from 'react'
import AddRegistry from './../components/AddRegistry'

export default class AddRegistryPage extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderAddRegistry(){
		return (
			<AddRegistry 
				standaloneMode={true}
			/>
		)
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Add Registry Credentials
				</h2>
				<div>
					{this.renderAddRegistry()}
				</div>
			</div>
		);
	}
}

AddRegistryPage.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

AddRegistryPage.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
import React, {Component} from 'react'

export default class Overview extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					TODO, detect repos
				</h2>
				<div>
					todo
				</div>
			</div>
		);
	}
}

Overview.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Overview.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
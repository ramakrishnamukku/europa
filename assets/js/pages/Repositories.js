import React, {Component} from 'react'
import Btn from './../components/Btn'
import ErrorMsg from './../components/ErrorMsg'

export default class Repositories extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Monitored Repositories
				</h2>
				<div>
					<Btn text="Add Repository"
							 onClick={ () => console.log("todo") } />
				</div>
			</div>
		);
	}
}

Repositories.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Repositories.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
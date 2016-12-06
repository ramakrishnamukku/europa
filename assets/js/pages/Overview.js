import React, {Component} from 'react'

export default class Overview extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}

	renderRepositories() {

	}

	render() {
		console.log(this.props)
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Repositories
				</h2>
				<div>
					{this.renderRepositories()}
				</div>
			</div>
		);
	}
}
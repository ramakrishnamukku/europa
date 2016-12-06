import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'

export default class Registries extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listRegistries();
	}
	renderRegistries(){
		return (
			<div className="">
				{this.context.state.registries.map((reg, index) => {
					return (
						<div key={index}>
							{reg.key}
						</div>
					);
				})}
			</div>
		);
	}
	render() {
		return (
			<div className="ContentContainer">
				<h2 className="PageHeader">
					Your Registries
				</h2>
				<div>
					{this.renderRegistries()}
				</div>
			</div>
		);
	}
}

Registries.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};

Registries.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object
};
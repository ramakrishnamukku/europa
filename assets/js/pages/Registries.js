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
				<div className="PageHeader">
					<h2>
						Registries
					</h2>
				</div>
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
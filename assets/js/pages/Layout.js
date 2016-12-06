import React, {Component} from 'react'
import {Link} from 'react-router'
import ActionBinder from './../util/ActionBinder'
// Actions
import * as AddRegistryActions from './../actions/AddRegistryActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);
		this.state = {
			registries: [],
			addRegistry: {
				...AddRegistryActions.addRegistryState()
			}
		};
	}
	getChildContext(){
		return {
			actions: ActionBinder([AddRegistryActions], this),
			state: this.state
		};
	}
	componentDidMount() {
		
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
					<h2>
						<Link to="/registries">Europa</Link>
					</h2>
				</nav>
				<div className="PageContent">
					<div className="MaxWidthContainer">
						{this.props.children}
					</div>
				</div>
			</div>
		);
	}
}


Layout.childContextTypes = {
	actions: React.PropTypes.object,
	state: React.PropTypes.object
};


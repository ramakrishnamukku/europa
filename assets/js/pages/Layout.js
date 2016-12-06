import React, {Component} from 'react'
import {Link} from 'react-router'
import ActionBinder from './../util/ActionBinder'
// Actions
import * as AddRegistryActions from './../actions/AddRegistryActions'

export default class Layout extends Component {
	constructor(props) {
		super(props);

		// Main State Store
		this.state = {
			registries: [],
			repositories: [],
			addRegistry: {
				...AddRegistryActions.addRegistryState()
			}
		};
	}
	getChildContext() {
		return {
			actions: ActionBinder([AddRegistryActions], this),
			state: this.state
		};
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
				 <div className="MaxWidthContainer">
					<h2>
						<Link to="/registries">Europa</Link>
					</h2>
					<div className="FlexRow NavButtonContainer">
						<div className="Flex1">
							<Link to="/repositories">
								<i className="icon icon-dis-package"/>
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/registries">
								<i className="icon icon-dis-docker" style={{fontSize: '1.4rem'}}/>
							</Link>
						</div>
						<div className="Flex1">
							<Link to="/settings">
								<i className="icon icon-dis-settings"/>
							</Link>
						</div>
					</div>
					</div>
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
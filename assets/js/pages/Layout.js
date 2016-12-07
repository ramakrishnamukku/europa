import React, {Component} from 'react'
import {Link} from 'react-router'
import ReactTooltip from 'react-tooltip'
import ActionBinder from './../util/ActionBinder'

// Actions
import * as AddRegistryActions from './../actions/AddRegistryActions'
import * as AddRepoActions from './../actions/AddRepoActions'
import * as RegistryActions from './../actions/RegistryActions'


export default class Layout extends Component {
	constructor(props) {
		super(props);

		// Main State Store
		this.state = {
			registries: [],
			repositories: [],
			registry: {
				...RegistryActions.registriesState(),
			},
			addRegistry: {
				...AddRegistryActions.addRegistryState(),
			},
			addRepo: {
				...AddRepoActions.addRepoState()
			}
		};
	}
	componentDidUpdate(prevProps, prevState) {
		ReactTooltip.rebuild()
	}
	getChildContext() {
		return {
			actions: ActionBinder([AddRegistryActions, AddRepoActions, RegistryActions], this),
			state: this.state
		};
	}
	render() {
		return (
			<div className="PageContainer">
				<nav className="TopNav">
				 <div className="MaxWidthContainer">
					<div className="logo">
						<Link to="/repositories">
							<img src="assets/images/distelli-europa-logo.svg"
									 alt="Distelli Europa" />
						</Link>
					</div>
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
						<ReactTooltip id="ToolTip" place="top" type="dark" effect="float"/>
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
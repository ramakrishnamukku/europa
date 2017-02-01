/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import RegistryNames from './../util/RegistryNames'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import NPECheck from './../util/NPECheck'
import Msg from './../components/Msg'

export default class CreateLocalRepo extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.refs['name'].focus();
	}
	createLocalRepo(){
		this.context.actions.createLocalRepo()
		.then(() => {
			this.context.router.push('/repositories');
		})
		.catch(() => {});
	}
	renderRepoNameInput(){
		return (
			<div className="RowPadding">
				<label>Repository Name</label>
				<input className="BlueBorder FullWidth" 
					   onChange={(e) => this.context.actions.updateNewLocalRepoName(e)}
					   placeholder="Enter repository name.."
					   ref="name"/>
			   {this.renderButton()}
			   {this.renderError()}
			</div>
		);
	}
	renderButton(){
		if(NPECheck(this.props, 'addRepo/createLocalXHR', false)) {
			return (
				<Loader />
			);
		}

		return (
			<Btn onClick={() => this.createLocalRepo()}
		         className="LargeBlueButton"
			  	 text="Create Repository"
			  	 style={{margin: ' 1rem auto'}}
			 	 canClick={true} />
		);
	}
	renderError(){
		let error = NPECheck(this.props, 'addRepo/createLocalError', false)
		if(error) {
			return (
				<Msg text={error} 
			     close={() => this.context.actions.clearCreateLocalRepoErrors()}/>
			);
		}
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Create New Repository
					</h2>
					<div className="FlexRow">
					</div>
				</div>
				<div>
					{this.renderRepoNameInput()}
				</div>
			</div>
		);
	}
}

CreateLocalRepo.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

CreateLocalRepo.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};
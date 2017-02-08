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
import CopyToClipboard from './../util/CopyToClipboard'

export default class CreateLocalRepo extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.refs['name'].focus();
	}
	componentWillUnmount() {
		this.context.actions.resetAddRepoState();
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
	renderCommands(){
		return (
			<div className="FlexColumn NewRepoCommands">
				<div>or</div>
				<div>Push a Docker image to a local repository</div>
				<p><strong>Command</strong> description dolor sit amet, cectetuer adipiscing elit, sed diam nonumy nibh euismod tincidunt ut laoreet dolore magna aliquam erat.</p>
				<div className="Code">
					 <span id="copyCommands">$ docker push {this.props.dnsName}/YOUR_NEW_REPO_NAME[:YOUR_IMAGE_TAG]</span>
					 <i className="icon icon-dis-copy" 
					 	onClick={() => CopyToClipboard(document.getElementById('copyCommands'))}
					 	data-tip="Click To Copy"
					 	data-for="ToolTipTop" />
				</div>
			</div>
		);
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
					{this.renderCommands()}
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
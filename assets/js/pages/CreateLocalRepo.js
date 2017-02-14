/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import { Link } from 'react-router'
import RegistryNames from './../util/RegistryNames'
import RegistryProviderIcons from './../util/RegistryProviderIcons'
import CenteredConfirm from './../components/CenteredConfirm'
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
		.then((res) => {
			this.context.router.push(`/repository/${res.id}`);
		})
		.catch(() => {});
	}
	renderRepoNameInput(){
		return (
			<div className="FlexColumn">
				<label style={{marginBottom: '5px'}}>Repository Name</label>
				<input className="BlueBorder FullWidth White" 
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
			<CenteredConfirm confirmButtonText="Create"
	                         noMessage={true}
	                         confirmButtonStyle={{}}
	                         onConfirm={() => this.createLocalRepo()}
	                         onCancel={() => this.context.actions.toggleCreateNewLocalRepo()} 
	                         containerStyle={{paddingBottom: '0px'}}/>
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
				<div className="HelperText">or</div>
				<div className="HelperText">Push a Docker image to a local repository</div>
				<div className="HelperText FlexRow">
					<div className="Code White">
						 <span>$ docker push <span id="copyCommands">{`${this.props.dnsName}/${(this.props.isLoggedIn) ? NPECheck(this.props, 'ctx/username', '') + '/': ''}REPO_NAME[:IMAGE_TAG]`}</span></span>
						 <i className="icon icon-dis-copy" 
						 	onClick={() => CopyToClipboard(document.getElementById('copyCommands'))}
						 	data-tip="Click To Copy"
						 	data-for="ToolTipTop" />
					</div>
				</div>
			</div>
		);
	}
	render() {
		return (
			<div className="CR_BodyContent">
				{this.renderRepoNameInput()}
				{this.renderCommands()}
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
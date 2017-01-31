/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component, PropTypes} from 'react'
import AddRegistry from './../components/AddRegistry'
import ContentRow from './../components/ContentRow'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import Loader from './../components/Loader'
import Dropdown from './../components/Dropdown'
import NPECheck from './../util/NPECheck'
import AddRepoNotification from './../components/AddRepoNotification'

let dockerRepoNameKey = 'repo/name';
let targetKey = "notification/target";
let typeKey = "notification/type";
let secretKey = "notification/secret";

export default class AddRepository extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillMount() {
		this.context.actions.listRegistries();
	}
	componentWillUnmount() {
		this.context.actions.resetAddRepoState();
		this.context.actions.resetNotifState();
	}
	inputClassName(selector){
		let hasSelector = this.props.addRepo.errorFields.includes(selector)
		if(hasSelector) {
			return "BlueBorder FullWidth Error";
		} else {
		    return "BlueBorder FullWidth";
		}
	}		
	renderAddRegistry(){
		return (
			<AddRegistry 
			    {...this.props}
				standaloneMode={false}
				isEdit={false}
			/>
		);
	}
	renderDockerRepository(){
		return (
			<div className="Row FlexColumn">
				<label  style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Select Repository
				</label>
				{this.renderDockerRepositoryInput()}
			</div>
		);
	}
	renderDockerRepositoryInput(){
		if(NPECheck(this.props, 'addRepo/reposInRegistry/length', false) || NPECheck(this.props, 'addRepo/reposInRegistryXHR', false)) {
			return (
				<Dropdown isOpen={this.props.addRepo.selectRepoDropdown}
						  toggleOpen={() => this.context.actions.toggleSelectRepoDropdown()}
						  listItems={this.props.addRepo.reposInRegistry} 
						  renderItem={(repo, index) => this.renderRepoInRegistryListItem(repo, index)}
						  filterFn={(item) => item.indexOf(this.props.addRepo.reposInRegistryQuery) > -1}
						  inputOnChange={(e) => this.context.actions.updateReposInRegisterQuery(e, false)}
						  inputPlaceholder="Search or enter repository name"
						  inputClassName={this.inputClassName(dockerRepoNameKey)}
						  inputValue={NPECheck(this.props, 'addRepo/newRepo/repo/name', '')} 
						  noItemsMessage="No Repositories Found"
						  XHR={NPECheck(this.props, 'addRepo/reposInRegistryXHR', true)}/>
			);
		} else {
			return (
				<input className={this.inputClassName(dockerRepoNameKey)} 
				       onChange={(e) => this.context.actions.updateNewRepoField(dockerRepoNameKey, e)}
				       placeholder="Search or enter repository name"/>
			);
		}
	}
	renderRepoInRegistryListItem(repo, index){
		let className = "ListItem";

		if(repo == NPECheck(this.props, 'addRepo/newRepo/repo/name', null)) {
			className += " Active";
		}		

		return (
			<div key={index} className={className} 
				 onClick={() => this.context.actions.updateNewRepoField(dockerRepoNameKey, repo, true)}>
				<span className="FlexRow">
					<span className="Cell">
						<span className="Label">{repo}</span>
					</span>
				</span>
			</div>
		);
	}	
	renderAddRepoNotification(){
		return (
			<AddRepoNotification {...this.props}/>
		);
	}
	renderErrorMsg(){
		if(this.props.addRepo.errorMsg) {
			return (
				<Msg
					text={this.props.addRepo.errorMsg}
					close={() => this.context.actions.clearAddRepoError()}
				/>
			);
		}
	}
	renderSuccessMsg(){
		if(this.props.addRepo.success) {

			let message = 'Successfullt added repository to monitor';

			return (
				<Msg text={message} 
				     isSuccess={true}
				     close={ () => this.context.actions.clearAddRepoSuccess() }/>
			);
		}
	}
	renderLoader(){
		return (
			<Loader />
		);
	}
	renderAddRepoButton(){
		let canAdd = this.context.actions.canAddRepo();

		return (
			<div className="FlexRow JustifyCenter RowPadding">
				<Btn className="LargeBlueButton"
					 onClick={ () => this.addRepo() }
					 text="Connect Repository"
					 canClick={canAdd} />
			</div>
		);
	}
	addRepo(){
		if(this.props.addRepo.newRepoCredsType == 'NEW') {
			this.context.actions.addRegistryRequest()
			.then((credId) => this.context.actions.selectCredsForNewRepo(null, credId))
			.then(() => this.context.actions.addRepoRequest(this.toRepoDetails.bind(this)))
			.catch(() => console.error('Add Registry Errors -- Skipping add repo'))
		} else {
			this.context.actions.addRepoRequest(this.toRepoDetails.bind(this));
		}
	}
	toRepoDetails(repoId){
		this.context.router.push(`/repository/${repoId}`);
	}
	renderAddRepository(){
		let rows = [{
			columns: [{
                icon:'icon icon-dis-repo',
                renderBody: this.renderDockerRepository.bind(this)
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-webhook-circle-solid',
                iconStyle: {position: 'relative', top: '-2px'},
                renderBody: this.renderAddRepoNotification.bind(this),
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderErrorMsg.bind(this),
                condition: this.props.addRepo.errorMsg
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderLoader.bind(this),
                condition: this.props.addRepo.XHR
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderSuccessMsg.bind(this),
                condition: this.props.addRepo.success
            }]
		}, {
			columns: [{
                icon:'icon icon-dis-blank',
                renderBody: this.renderAddRepoButton.bind(this)
            }]
		}];

		return rows.map(this.renderContentRow);
	}
	renderContentRow(row, index){
		return (
			<ContentRow key={index}
						row={row} />
		);	
	}
	render() {
		return (
			<div className="ContentContainer">
				<div className="PageHeader">
					<h2>
						Connect a repository
					</h2>
				</div>
				<div>
				    {this.renderAddRegistry()}
					{this.renderAddRepository()}
				</div>
			</div>
		);
	}
}

AddRepository.childContextTypes = {
	actions: PropTypes.object,
    router: PropTypes.object
};

AddRepository.contextTypes = {
	actions: PropTypes.object,
    router: PropTypes.object
};

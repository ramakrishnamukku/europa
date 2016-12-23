/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {Component} from 'react'
import ContentRow from './../components/ContentRow'
import Loader from './../components/Loader'
import Btn from './../components/Btn'
import Msg from './../components/Msg'
import RadioButton from './../components/RadioButton'
import RegistryNames from './../util/RegistryNames'
import RegistryProviderIcons from './../util/RegistryProviderIcons'

let provider = 'provider';
let keyName = 'name';
let region = 'region';
let key = 'key';
let secret = 'secret';

export default class AddRegistry extends Component {
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentWillUnmount() {
		this.context.actions.resetAddRegistryState();	
	}
	inputClassName(selector){
		let hasSelector = this.context.state.addRegistry.errorFields.includes(selector)
		let className;
		if(hasSelector) {
			className = "BlueBorder FullWidth Error";
		} else {
		    className = "BlueBorder FullWidth";
		}

		if(this.props.standaloneMode) {
			className += ' White';
		} else {
			className += ' Dark';
		}

		return className;
	}
	renderRegistryCredentials() {
		return (
			<div className="FlexColumn">
				{this.renderLabel()}
				{this.renderChooseCredsTypes()}
				{this.renderNewRegistryCredentials()}
				{this.renderExistingRegistryCredentials()}
			</div>
		);
	}
	renderLabel(){
		if(!this.props.standaloneMode) {
			return (
				<label>
					Registry Credentials
				</label>
			);
		}
	}
	renderChooseCredsTypes(){
		if(!this.props.standaloneMode) {
			return (
				<div className="FlexRow RowPadding">
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('EXISTING')} 
									 isChecked={this.context.state.addRepo.newRepoCredsType == 'EXISTING'}
									 label="Existing Credentials" />
					</div>
					<div className="Column">
						<RadioButton onClick={() => this.context.actions.setNewRepoCredsType('NEW')} 
							  		 isChecked={this.context.state.addRepo.newRepoCredsType == 'NEW'}
									 label="New Credentials" />
					</div>
				</div>
			);
		}
	}
	renderExistingRegistryCredentials() {
		if(this.context.state.addRepo.newRepoCredsType == 'EXISTING' && !this.props.standaloneMode) {
			return (
				<div className="Flex1">
					<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
						Select Credentials
					</label>
					<div className="Flex1">
						<select className="BlueBorder FullWidth"
						        onChange={(e) => this.context.actions.selectCredsForNewRepo(e)}>
						    <option value="">Select Credentials</option>
							{this.context.state.registries.map((reg, index) => {
								return (
									<option value={JSON.stringify(reg)} key={index}>{reg.provider} - {reg.name} - {reg.region}</option>
								);
							})}
						</select>
					</div>
				</div>
			);
		}
	}
	renderSelectProvider(readOnly, isEdit){
		if(isEdit) {
			return (
				<div className="Flex1 FlexRow">
					<img src={RegistryProviderIcons(this.context.state.addRegistry.newRegistry[provider])} />
				</div>
			);
		}

		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Docker Registry Provider {(this.props.isEdit) ? '( Read Only )' : null}
				</label>
				<select className={this.inputClassName(provider)}
						value={this.context.state.addRegistry.newRegistry[provider]}
				        onChange={(e) => this.context.actions.updateNewRegistryField(provider, e)}
		       			{...readOnly}>
				   <option value="">Select Provider</option>
				   {Object.keys(RegistryNames).map((key) => {
				   		return (
				   			<option key={key} value={key}>{RegistryNames[key]}</option>
				   		);
				   })}
				</select>
			</div>
		);
	}
	renderInputKeyName(readOnly, isEdit){
		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Key Name {(this.props.isEdit) ? '( Read Only )' : null}
				</label>
				<input className={this.inputClassName(keyName)}
					   value={this.context.state.addRegistry.newRegistry[keyName]}
				       placeholder="Enter Key Name.."
					   onChange={(e) => this.context.actions.updateNewRegistryField(keyName, e)} 
					   {...readOnly}/>
			</div>
		);
	}
	renderInputPublicKey(readOnly, isEdit){
		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Public Key
				</label>
				<input className={this.inputClassName(key)}
					   value={this.context.state.addRegistry.newRegistry[key]}
					   placeholder="Enter Public Key.."
					   onChange={(e) => this.context.actions.updateNewRegistryField(key, e)} />
			</div>
		);
	}
	renderInputPrivateKey(readOnly, isEdit){
		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Private Key
				</label>
				<input className={this.inputClassName(secret)}
					   value={this.context.state.addRegistry.newRegistry[secret]}
					   placeholder="Enter Private Key.."
					   onChange={(e) => this.context.actions.updateNewRegistryField(secret, e)} />
			</div>
		);
	}
	renderSelectRegion(readOnly, isEdit){
		return (
			<div className="Flex1">
				<label className="small" style={(this.props.standaloneMode) ? {display: 'none'} : {}}>
					Key Region {(this.props.isEdit) ? '( Read Only )' : null}
				</label>
				<select className={this.inputClassName(region)}
						value={this.context.state.addRegistry.newRegistry[region]}
				        onChange={(e) => this.context.actions.updateNewRegistryField(region, e)}
						{...readOnly}>
				   <option value="">Select Region...</option>
				   <option value="us-west-1">us-west-1</option>
				   <option value="us-west-2">us-west-2</option>
				   <option value="us-east-1">us-east-1</option>
				   <option value="us-east-2">us-east-2</option>
				</select>
			</div>
		);
	}
	renderNewRegistryCredentials(){
		if(this.context.state.addRepo.newRepoCredsType == 'NEW' || this.props.standaloneMode) {

			let isEdit = this.props.isEdit;
			let readOnly = {};

			if(isEdit) {
				readOnly['readOnly'] = 'readOnly';
				readOnly['disabled'] = 'disabled';
			}

			return (
				<div className="AddEditRegistryCreds" style={this.props.standaloneMode ? {} : {margin: '0 -10px'}}>
					{this.renderSelectProvider(readOnly, isEdit)}
					{this.renderInputKeyName(readOnly, isEdit)}
					{this.renderInputPublicKey(readOnly, isEdit)}
					{this.renderInputPrivateKey(readOnly, isEdit)}
					{this.renderSelectRegion(readOnly, isEdit)}
				</div>
			);
		}
	}
	renderErrorMsg(){
		if(this.context.state.addRegistry.errorMsg) {
			return (
				<Msg
					text={this.context.state.addRegistry.errorMsg}
				/>
			);
		}
	}
	renderSuccessMsg(){
		if(this.context.state.addRegistry.success) {

			let message = `Successfully ${(this.props.isEdit) ? 'updated' : 'added'} registry credentials`;

			return (
				<Msg text={message} 
				     isSuccess={true}
				     close={() => this.context.actions.clearAddRegistrySuccess()}/>
			);
		}
	}
	renderLoader(){
		return (
			<Loader />
		);
	}
	renderActions(){
		return (
			<div className="FlexRow JustifyCenter" style={{margin: '0 auto', width: '300px'}}>
				<div className="Flex1" style={{margin: '0px 10px'}}>
					<Btn onClick={() => this.context.actions.addRegistryRequest()}
						 text={(this.props.isEdit) ? 'Save Registry' : 'Add Registry'}
						 canClick={this.context.actions.canAddRegistry()} />
					</div>
				<div className="Flex1" style={{margin: '0px 10px'}}>
					<Btn onClick={ () => this.context.actions.toggleShowAddEditRegistryModal() }
						 className="Btn Cancel"
						 text="Cancel"
						 canClick={true} />
				</div>
			</div>
		);
	}
	renderAddRegistry(){
		let rows = [{
			columns: [{
				icon: (this.props.standaloneMode) ? null : 'icon icon-dis-credential',
                renderBody: this.renderRegistryCredentials.bind(this)
            }]
		}, {
			columns: [{
				icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
                renderBody: this.renderErrorMsg.bind(this),
                condition: this.context.state.addRegistry.errorMsg
            }]
		}];

		if(this.props.standaloneMode) {
			let standAloneRows = [{
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderLoader.bind(this),
	                condition: this.context.state.addRegistry.XHR
	            }]
			}, {
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderSuccessMsg.bind(this),
	                condition: this.context.state.addRegistry.success
	            }]
			}, {
				columns: [{
					icon: (this.props.standaloneMode) ? null : 'icon icon-dis-blank',
	                renderBody: this.renderActions.bind(this),
	                condition: this.props.standaloneMode
	            }]
			}]

			rows = rows.concat(standAloneRows);
		}

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
				<div>
					{this.renderAddRegistry()}
				</div>
			</div>
		);
	}
}

AddRegistry.propTypes = {
	standaloneMode: React.PropTypes.bool,
	isEdit: React.PropTypes.bool
};

AddRegistry.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

AddRegistry.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};
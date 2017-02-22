/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {PropTypes, Component} from 'react'
import ControlRoom from './../components/ControlRoom'
import CenteredConfirm from './../components/CenteredConfirm'
import Btn from './../components/Btn'
import Loader from './../components/Loader'
import Msg from './../components/Msg'
import NPECheck from './../util/NPECheck'
import ConvertTimeUTC from './../util/ConvertTimeUTC'
import CopyToClipboard from './../util/CopyToClipboard'

export default class APITokens extends Component{
	constructor(props) {
		super(props);
		this.state = {};
	}
	componentDidMount() {
		this.context.actions.listAuthTokens();
	}
	createAuthToken(){
		this.context.actions.createAuthToken()
		.then(this.context.actions.listAuthTokens);
	}
	setAuthTokenStatus(tokenString, status){
		this.context.actions.setAuthTokenStatus(tokenString, status)
		.then(this.context.actions.listAuthTokens);
	}
	deleteAuthToken(){
		this.context.actions.deleteAuthToken()
		.then(this.context.actions.listAuthTokens);	
	}
	renderHeader(){
		return (
			<div className="APIHeader">
			 	<span className="Flex2">
			 		API Tokens
			 	</span>
			 	<span className="Flex1" style={(this.props.isLoggedIn) ? {} : {display: 'none'}}>
			 		Owner
			 	</span>
			 	<span className="Flex1">
			 		Created
			 	</span>
			 	<span style={{flex: '0.7'}}>
			 		Status
			 	</span>
			 	<span className="ThickBlueText Actions" onClick={() => this.createAuthToken()}>
			 		{this.renderCreateTokenText()}
			 	</span>
			</div>
		);
	}
	renderCreateTokenText(){
		if(NPECheck(this.props, 'settings/tokens/createTokenXHR', false)) {
			return (
				<i className="icon icon-dis-waiting rotating" />
			);
		}

		return '+ Create Token';
	}
	renderContent(){
		let errorMsg = NPECheck(this.props, 'settings/tokens/tokenPageError', false);
		if(errorMsg) {
			return this.renderError(errorMsg);
		}

		let tokens = NPECheck(this.props, 'settings/tokens/allTokens', []);
		return (
			<div className="APIBody">
				{tokens.sort((firstEvent, secondEvent) => (firstEvent.created > secondEvent.created) ? -1 : 1 ).map((token, i) => {
					let statusClassName = (token.status == 'ACTIVE') ? 'Active' : 'Inactive';
					 let dateCreated = new Date(token.created);
					 let dateCreatedUTC = ConvertTimeUTC(dateCreated);

					return (
						<div className="TokenItem" key={i}>
							<div className="TokenDetails">
								{this.renderTokenString(token.token)}
								<span className="Flex1" style={(token.username) ? {} : {display: 'none'}}>{token.username}</span>
								<span className="Flex1">{dateCreatedUTC}</span>
								<span className={statusClassName} style={{flex: '0.7'}}>{token.status}</span>
								{this.renderIcons(token)}
							</div>
							{this.renderDeleteToken(token)}
						</div>
					);
				})}				
			</div>
		);
	}
	renderIcons(token){
		let statusToolTip = 'Deactivate Token';
		let newStatus = 'INACTIVE';
		let statusIcon = 'icon icon-dis-inactive';
		let contents = [];
		
		if(token.status == 'INACTIVE') {
			statusToolTip = 'Activate Token';
			newStatus = 'ACTIVE';			
			statusIcon = 'icon icon-dis-active';
		}

		let infoIconDOM = (
			 <i className='icon icon-dis-questionmark' key={1}
				onClick={() => {}} data-tip="A token must be INACTIVE to be deleted" data-for="ToolTipTop"/>
		);

		let deleteIconDOM = (
			<i className='icon icon-dis-trash' key={1}
				onClick={() => this.context.actions.toggleTokenForDelete(token.token)} data-tip="Delete Token" data-for="ToolTipTop"/>
		);

		let statusIconDOM = (	
			<i className={statusIcon} key={2}
				onClick={() => this.setAuthTokenStatus(token.token, newStatus)} data-tip={statusToolTip} data-for="ToolTipTop"/>
		);			

		contents.push(statusIconDOM);
		contents.push((token.status == 'INACTIVE') ? deleteIconDOM : infoIconDOM );

		return (
			<span className="Flex1 Actions">
				{contents}
			</span>
		);
	}
	renderTokenString(tokenString){
		let displayToken = '••••••••••••••••••••';
		let verb = 'Show';
		let isActive = NPECheck(this.props, 'settings/tokens/showingTokens').includes(tokenString);

		if(isActive) {
			verb = 'Hide';
			displayToken  = tokenString;
		}

		return (
			<span className="Flex2 Token">
				<div style={{width: '180px'}}>
					<span id={tokenString} style={{display: 'none'}}>{tokenString}</span>
					<span className="Flex1">{displayToken}</span>
				</div>
				<div className="ItalicText">
					 <span className="Pipe">|</span>
					 <span className="ItalicItem" onClick={() => this.context.actions.toggleShowingToken(tokenString)}>{verb}</span>&nbsp;&nbsp; 
					 <span className="ItalicItem" onClick={() => CopyToClipboard(document.getElementById(tokenString))}>Copy</span>
				</div>
			</span>
		);

	}
	renderDeleteToken(token){
		if( NPECheck(this.props, 'settings/tokens/selectedTokenForStatusUpdate', null) == token.token 
			|| NPECheck(this.props, 'settings/tokens/selectedTokenForDelete', null) ==  token.token) {

			let errorMsg = NPECheck(this.props, 'settings/tokens/tokenItemError', null);
			if(errorMsg) return this.renderError(errorMsg);

			if( NPECheck(this.props, 'settings/tokens/statusXHR', false)
			    || NPECheck(this.props, 'settings/tokens/deleteTokenXHR', false)) {
				return (
					<Loader />
				);
			}

			return (
				<CenteredConfirm message="Are you sure you want to delete this token? This is permanent"
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => this.deleteAuthToken()}
							     onCancel={() => this.context.actions.toggleTokenForDelete() }/>
			);
		}
	}
	renderError(errorMsg){
		return (
			<Msg text={errorMsg} 
				 close={() => this.context.actions.clearTokenItemError()}
				 style={{padding: '1rem 0'}}/>
		);
	}
	renderPageContent(){
		let isLoading = NPECheck(this.props, 'settings/tokens/tokensXHR', false);
		if(isLoading) {
			return (
				<div className="PageLoader">
					<Loader />
				</div>
			);
		}				

		let tokens = NPECheck(this.props, 'settings/tokens/allTokens', []);
		if(!tokens.length) {
			return (
				<div className="NoContent">
					<h3>No API Tokens found.</h3>
					<Btn className="LargeBlueButton"
						 text="Create Token"
						 onClick={() => this.createAuthToken()}/>
				</div>
			);
		}

		return (
			<ControlRoom renderHeaderContent={() => this.renderHeader()}
						 renderBodyContent={() => this.renderContent()} />
		);
	}
	render(){
		return (
			<div className="APITokens">
				{this.renderPageContent()}
			</div>	
		);
	}
}

APITokens.childContextTypes = {
    actions: React.PropTypes.object
};

APITokens.contextTypes = {
    actions: React.PropTypes.object
};



/*
  @author Sam Heutmaker [samheutmaker@gmail.com]
*/

import React, {PropTypes, Component} from 'react'
import ControlRoom from './../components/ControlRoom'
import CenteredConfirm from './../components/CenteredConfirm'
import Btn from './../components/Btn'
import NPECheck from './../util/NPECheck'

export default class APITokens extends Component{
	constructor(props) {
		super(props);
		this.state = {};
	}
	renderHeader(){
		return (
			<div className="APIHeader">
			 	<span className="Flex2">
			 		API Tokens
			 	</span>
			 	<span className="Flex2">
			 		Description
			 	</span>
			 	<span className="Flex1">
			 		Created
			 	</span>
			 	<span className="Flex2">
			 		Status
			 	</span>
			 	
			</div>
		);
	}
	renderContent(){
		let tokens = [{
			token: 'token',
			desc: 'description',
			status: 'status',
			created: 'created'
		}, {
			token: 'token1',
			desc: 'description',
			status: 'status',
			created: 'created'
		}, {
			token: 'token2',
			desc: 'description',
			status: 'status',
			created: 'created'
		}, {
			token: 'token3',
			desc: 'description',
			status: 'status',
			created: 'created'
		}];

		return (
			<div className="APIBody">
				{tokens.map((token, i) => {
					return (
						<div className="TokenItem" key={i}>
							<div className="TokenDetails">
								{this.renderTokenString(token.token)}
								<span className="Flex2">{token.desc}</span>
								<span className="Flex1">{token.status}</span>
								<span className="Flex1">{token.created}</span>
								<span className="Flex1 Delete">
									<i className='icon icon-dis-trash' onClick={() => this.context.actions.toggleTokenForDelete(token.token)}/>
								</span>
							</div>
							{this.renderDeleteToken(token)}
						</div>
					);
				})}				
			</div>
		);
	}
	renderTokenString(tokenString){
		let displayToken = '*******************';
		let verb = 'Show';
		let isActive = NPECheck(this.context.state, 'settings/tokens/showingTokens').includes(tokenString);

		if(isActive) {
			verb = 'Hide';
			displayToken  = tokenString;
		}

		return (
			<span className="Flex2 Token">
				<span className="Flex1">
					{displayToken}
				</span>
				<Btn onClick={() => this.context.actions.toggleShowingToken(tokenString)}
					 text={verb} />
			</span>
		);

	}
	renderDeleteToken(token){
		if(NPECheck(this.context.state, 'settings/tokens/selectedTokenForDelete') ==  token.token) {
			return (
				<CenteredConfirm message="Are you sure you want to delete this token? This is permanent"
							     confirmButtonText="Delete"
							     confirmButtonStyle={{}}
							     onConfirm={() => console.log('Delete')}
							     onCancel={() => this.context.actions.toggleTokenForDelete() }/>
				);
		}
	}
	renderControlRoom(){
		return (
			<ControlRoom renderHeaderContent={() => this.renderHeader()}
						 renderBodyContent={() => this.renderContent()} />
		);
	}
	render(){
		return (
			<div className="APITokens">
				{this.renderControlRoom()}
			</div>	
		);
	}
}


APITokens.childContextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};

APITokens.contextTypes = {
    actions: React.PropTypes.object,
    state: React.PropTypes.object,
    router: React.PropTypes.object
};



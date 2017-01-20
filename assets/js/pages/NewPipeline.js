import React, {Component, PropTypes} from 'react'
import Btn from './../components/Btn'

export default class NewPipeline extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  toHome(){
    this.context.router.push('/repositories');
  }
  render() {
    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
            NewPipeline
          </h2>
        </div>
        <div>
          <Btn onClick={() => this.toHome()}
             className="LargeBlueButton"
             text="Take Me Home"
             style={{marginTop: '21px'}}
             canClick={true}/>
        </div>
      </div>
    );
  }
}

NewPipeline.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

NewPipeline.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

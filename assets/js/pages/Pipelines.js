import React, {Component, PropTypes} from 'react'
import Btn from './../components/Btn'

export default class Pipelines extends Component {
  constructor(props) {
    super(props);
    this.state = {};
  }
  toHome(){
    this.context.router.push('/');
  }
  render() {
    return (
      <div className="ContentContainer">
        <div className="PageHeader">
          <h2>
            Pipelines
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

Pipelines.childContextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

Pipelines.contextTypes = {
    actions: PropTypes.object,
    router: PropTypes.object
};

/**
 * Created by lucasmf on 2/25/15.
 */
React = require('react');
ReactBootstrap = require('react-bootstrap');
Navbar = ReactBootstrap.Navbar;
Nav = ReactBootstrap.Nav;
NavItem = ReactBootstrap.NavItem;
Header = React.createClass({
    render: function () {
        return (
            <Navbar>
                <Nav>
                    <NavItem>話語補聽</NavItem>
                    <NavItem>話語搜尋</NavItem>
                    <NavItem>宣教表格</NavItem>

                </Nav>
            </Navbar>
        );
    }
});
module.exports = Header;
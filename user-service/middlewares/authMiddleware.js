const asyncHandler = require("express-async-handler");
const jwt = require('jsonwebtoken');
require('dotenv').config();
const { models: { User } } = require("../models");
const protect = async (req, res, next) => {
    let token;
    console.log('protected routes')
    if(req.headers.authorization.startsWith("Bearer ")) {
        token = req.headers.authorization.split(" ")[1];
    }
    else if(req.cookies.jwt) {
        token = req.cookies.jwt;
    }else { 
        return res.status(401).json({message: "No token provided"})
    }
    console.log("token: ", token);
    try {
        const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);
        console.log("Decoded:", decoded);

        const user = await User.findByPk(decoded.userId);  // NOW WORKS
        console.log(user);

        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        req.user = user;
        next();

    } catch (err) {
        console.error(err);
        return res.status(401).json({ message: "Invalid token" });
    }
}

const protectAdmin = asyncHandler(async (req, res, next) => {
    let token;
    console.log("protect admin")

    if (req.headers.authorization?.startsWith("Bearer ")) {
        token = req.headers.authorization.split(" ")[1];
    } else if (req.cookies.jwt) {
        token = req.cookies.jwt;
    } else {
        return res.status(401).json({ message: "No token provided" });
    }

    try {
        const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);
        console.log("Decoded:", decoded);

        const user = await User.findByPk(decoded.userId);  // NOW WORKS

        if (!user) {
            return res.status(404).json({ message: "User not found" });
        }

        if (user.role !== "admin") {
            return res.status(403).json({ message: "Admin-only route" });
        }

        req.user = user;
        next();

    } catch (err) {
        console.error(err);
        return res.status(401).json({ message: "Invalid token" });
    }
})

const protectEditor = asyncHandler(async (req, res, next) => {
    let token;

    if(req.headers.authorization.startsWith('Bearer ')) {
        token = req.headers.authorization.split(" ")[1];
    }
    else if (req.cookies.jwt) {
        token = req.cookies.jwt;
    }
    
    try {
        const decoded = jwt.verify(token, process.env.ACCESS_TOKEN_SECRET);

        const user = await User.findByPk(decoded.userId);

        if (!user) return res.status(401).json({ message: "user not found" });

        if(user.role !== 'editor' && user.role !=='admin') {
            return res.status(403).json({message: 'editor only route'});
        }

        req.user = user;
        next();
    } catch (err) {
        return res.status(401).json({ message: "Invalid token" });
    }
});


module.exports = {protect, protectAdmin, protectEditor};
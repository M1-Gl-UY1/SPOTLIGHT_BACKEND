const router = require("express").Router();
const {
  getAll,
  getById,
  create,
  update,
  remove,
  updateRating
} = require("../controllers/providerController");
const { protectAdmin, protectEditor } = require("../middlewares/authMiddleware");

router
  .route("/")
  .get(protectEditor, getAll)
  .post(protectAdmin, create);

router
  .route("/:id")
  .get(protectEditor, getById)
  .put(protectAdmin, update)
  .delete(protectAdmin, remove);

router
  .route("/:id/rating")
  .patch(protectEditor, updateRating);

module.exports = router;
